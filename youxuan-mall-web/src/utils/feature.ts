import { ElMessage } from 'element-plus'

export const showBackendTodo = (feature = '') => {
  ElMessage.info(feature ? `${feature}后端功能暂未实现` : '后端功能暂未实现')
}
