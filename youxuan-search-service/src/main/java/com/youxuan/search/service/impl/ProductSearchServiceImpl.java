package com.youxuan.search.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.youxuan.common.exception.BusinessException;
import com.youxuan.common.result.ErrorCode;
import com.youxuan.common.result.PageResult;
import com.youxuan.common.result.Result;
import com.youxuan.search.client.ProductClient;
import com.youxuan.search.client.vo.ProductClientVO;
import com.youxuan.search.document.ProductDocument;
import com.youxuan.search.dto.ProductSearchRequest;
import com.youxuan.search.service.ProductSearchService;
import com.youxuan.search.vo.ProductSearchVO;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.ResponseException;
import org.elasticsearch.client.RestClient;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * 商品搜索实现，阶段 8 仅支持手动全量导入和查询，不做 MQ 自动同步。
 */
@Service
public class ProductSearchServiceImpl implements ProductSearchService {

    private static final String INDEX_NAME = "youxuan_product";
    private static final int STATUS_ON = 1;
    private static final long IMPORT_PAGE_SIZE = 100L;
    private static final long MAX_PAGE_SIZE = 100L;

    private final ProductClient productClient;
    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    public ProductSearchServiceImpl(ProductClient productClient, RestClient restClient, ObjectMapper objectMapper) {
        this.productClient = productClient;
        this.restClient = restClient;
        this.objectMapper = objectMapper;
    }

    /**
     * 手动全量导入：重建索引后分页拉取商品服务数据，避免旧 ES 文档残留。
     */
    @Override
    public Integer importAll() {
        try {
            recreateIndex();
            int importedCount = 0;
            long pageNum = 1L;
            while (true) {
                PageResult<ProductClientVO> page = fetchProductPage(pageNum);
                List<ProductClientVO> records = page.getRecords();
                if (records == null || records.isEmpty()) {
                    break;
                }
                bulkIndex(records);
                importedCount += records.size();
                if (page.getPages() == null || pageNum >= page.getPages()) {
                    break;
                }
                pageNum++;
            }
            return importedCount;
        } catch (IOException exception) {
            throw new BusinessException(ErrorCode.BUSINESS_ERROR, "导入商品索引失败：" + exception.getMessage());
        }
    }

    @Override
    public PageResult<ProductSearchVO> search(ProductSearchRequest request) {
        ProductSearchRequest safeRequest = request == null ? new ProductSearchRequest() : request;
        long pageNum = safeRequest.getPageNum() == null || safeRequest.getPageNum() < 1 ? 1L : safeRequest.getPageNum();
        long pageSize = safeRequest.getPageSize() == null || safeRequest.getPageSize() < 1
                ? 10L : Math.min(safeRequest.getPageSize(), MAX_PAGE_SIZE);

        try {
            ObjectNode searchBody = buildSearchBody(safeRequest, pageNum, pageSize);
            Request esRequest = new Request("POST", "/" + INDEX_NAME + "/_search");
            esRequest.setJsonEntity(objectMapper.writeValueAsString(searchBody));
            JsonNode response = performJson(esRequest);

            JsonNode hitsNode = response.path("hits");
            long total = hitsNode.path("total").path("value").asLong(0L);
            List<ProductSearchVO> records = new ArrayList<>();
            for (JsonNode hit : hitsNode.path("hits")) {
                records.add(ProductSearchVO.fromSource(hit.path("_source")));
            }
            return PageResult.of(total, pageNum, pageSize, records);
        } catch (ResponseException exception) {
            if (exception.getResponse().getStatusLine().getStatusCode() == 404) {
                throw new BusinessException(ErrorCode.BUSINESS_ERROR, "商品索引不存在，请先调用导入接口");
            }
            throw new BusinessException(ErrorCode.BUSINESS_ERROR, "商品搜索失败：" + exception.getMessage());
        } catch (IOException exception) {
            throw new BusinessException(ErrorCode.BUSINESS_ERROR, "商品搜索失败：" + exception.getMessage());
        }
    }

    private PageResult<ProductClientVO> fetchProductPage(long pageNum) {
        Result<PageResult<ProductClientVO>> result = productClient.page(pageNum, IMPORT_PAGE_SIZE);
        if (result == null || !Integer.valueOf(200).equals(result.getCode()) || result.getData() == null) {
            throw new BusinessException(ErrorCode.BUSINESS_ERROR, "调用商品服务分页接口失败");
        }
        return result.getData();
    }

    private void recreateIndex() throws IOException {
        if (indexExists()) {
            restClient.performRequest(new Request("DELETE", "/" + INDEX_NAME));
        }
        Request request = new Request("PUT", "/" + INDEX_NAME);
        request.setJsonEntity(buildIndexMapping());
        restClient.performRequest(request);
    }

    private boolean indexExists() throws IOException {
        try {
            Response response = restClient.performRequest(new Request("HEAD", "/" + INDEX_NAME));
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode == 404) {
                return false;
            }
            return statusCode >= 200 && statusCode < 300;
        } catch (ResponseException exception) {
            if (exception.getResponse().getStatusLine().getStatusCode() == 404) {
                return false;
            }
            throw exception;
        }
    }

    private String buildIndexMapping() {
        return """
                {
                  "mappings": {
                    "properties": {
                      "id": { "type": "long" },
                      "categoryId": { "type": "long" },
                      "categoryName": { "type": "keyword" },
                      "name": {
                        "type": "text",
                        "fields": { "keyword": { "type": "keyword", "ignore_above": 256 } }
                      },
                      "subtitle": {
                        "type": "text",
                        "fields": { "keyword": { "type": "keyword", "ignore_above": 256 } }
                      },
                      "mainImage": { "type": "keyword" },
                      "price": { "type": "double" },
                      "originalPrice": { "type": "double" },
                      "sales": { "type": "integer" },
                      "status": { "type": "integer" },
                      "createTime": { "type": "date" }
                    }
                  }
                }
                """;
    }

    private void bulkIndex(List<ProductClientVO> products) throws IOException {
        StringBuilder builder = new StringBuilder();
        for (ProductClientVO product : products) {
            ProductDocument document = ProductDocument.from(product);
            ObjectNode action = objectMapper.createObjectNode();
            ObjectNode index = action.putObject("index");
            index.put("_index", INDEX_NAME);
            index.put("_id", String.valueOf(document.getId()));
            builder.append(objectMapper.writeValueAsString(action)).append('\n');
            builder.append(objectMapper.writeValueAsString(document)).append('\n');
        }

        Request request = new Request("POST", "/_bulk");
        request.addParameter("refresh", "true");
        request.setEntity(new StringEntity(builder.toString(),
                ContentType.create("application/x-ndjson", StandardCharsets.UTF_8)));
        JsonNode response = performJson(request);
        if (response.path("errors").asBoolean(false)) {
            throw new BusinessException(ErrorCode.BUSINESS_ERROR, "批量写入商品索引失败");
        }
    }

    private ObjectNode buildSearchBody(ProductSearchRequest request, long pageNum, long pageSize) {
        ObjectNode root = objectMapper.createObjectNode();
        root.put("from", (pageNum - 1) * pageSize);
        root.put("size", pageSize);

        ObjectNode bool = root.putObject("query").putObject("bool");
        ArrayNode must = bool.putArray("must");
        ArrayNode filter = bool.putArray("filter");

        if (StringUtils.hasText(request.getKeyword())) {
            String keyword = request.getKeyword().trim();
            ObjectNode keywordBool = objectMapper.createObjectNode();
            ArrayNode should = keywordBool.putArray("should");

            ObjectNode multiMatch = objectMapper.createObjectNode();
            multiMatch.put("query", keyword);
            ArrayNode fields = multiMatch.putArray("fields");
            fields.add("name");
            fields.add("subtitle");
            should.add(objectMapper.createObjectNode().set("multi_match", multiMatch));

            // 未安装中文分词插件时，用 keyword 通配补足“耳机”等中文短词命中能力。
            should.add(buildWildcardQuery("name.keyword", keyword));
            should.add(buildWildcardQuery("subtitle.keyword", keyword));
            keywordBool.put("minimum_should_match", 1);
            must.add(objectMapper.createObjectNode().set("bool", keywordBool));
        } else {
            must.add(objectMapper.createObjectNode().set("match_all", objectMapper.createObjectNode()));
        }

        filter.add(objectMapper.createObjectNode()
                .set("term", objectMapper.createObjectNode().put("status", STATUS_ON)));
        if (request.getCategoryId() != null) {
            filter.add(objectMapper.createObjectNode()
                    .set("term", objectMapper.createObjectNode().put("categoryId", request.getCategoryId())));
        }
        if (request.getMinPrice() != null || request.getMaxPrice() != null) {
            ObjectNode priceRange = objectMapper.createObjectNode();
            if (request.getMinPrice() != null) {
                priceRange.put("gte", request.getMinPrice());
            }
            if (request.getMaxPrice() != null) {
                priceRange.put("lte", request.getMaxPrice());
            }
            filter.add(objectMapper.createObjectNode()
                    .set("range", objectMapper.createObjectNode().set("price", priceRange)));
        }

        ArrayNode sorts = root.putArray("sort");
        addSorts(sorts, request);
        return root;
    }

    private void addSorts(ArrayNode sorts, ProductSearchRequest request) {
        String sortField = normalizeSortField(request.getSortField());
        String sortOrder = normalizeSortOrder(request.getSortOrder());
        if (sortField == null) {
            addSort(sorts, "sales", "desc");
            addSort(sorts, "id", "desc");
            return;
        }
        addSort(sorts, sortField, sortOrder);
        addSort(sorts, "id", "desc");
    }

    private String normalizeSortField(String sortField) {
        if (!StringUtils.hasText(sortField)) {
            return null;
        }
        String field = sortField.trim();
        if ("sales".equals(field) || "price".equals(field) || "createTime".equals(field)) {
            return field;
        }
        throw new BusinessException(ErrorCode.PARAM_ERROR, "sortField 仅支持 sales、price、createTime");
    }

    private String normalizeSortOrder(String sortOrder) {
        if (!StringUtils.hasText(sortOrder)) {
            return "desc";
        }
        String order = sortOrder.trim().toLowerCase();
        if ("asc".equals(order) || "desc".equals(order)) {
            return order;
        }
        throw new BusinessException(ErrorCode.PARAM_ERROR, "sortOrder 仅支持 asc、desc");
    }

    private void addSort(ArrayNode sorts, String field, String order) {
        ObjectNode sortConfig = objectMapper.createObjectNode();
        sortConfig.set(field, objectMapper.createObjectNode().put("order", order));
        sorts.add(sortConfig);
    }

    private ObjectNode buildWildcardQuery(String field, String keyword) {
        ObjectNode wildcardConfig = objectMapper.createObjectNode();
        wildcardConfig.put("value", "*" + keyword + "*");
        ObjectNode wildcard = objectMapper.createObjectNode();
        wildcard.set(field, wildcardConfig);
        return objectMapper.createObjectNode().set("wildcard", wildcard);
    }

    private JsonNode performJson(Request request) throws IOException {
        Response response = restClient.performRequest(request);
        String body = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
        try {
            return objectMapper.readTree(body);
        } catch (JsonProcessingException exception) {
            throw new BusinessException(ErrorCode.BUSINESS_ERROR, "解析 Elasticsearch 响应失败");
        }
    }
}
