export type CouponCardState = 'claimable' | 'usable' | 'expiring' | 'used' | 'expired' | 'disabled' | 'soldOut'

export type CouponCardTone = 'green' | 'orange' | 'red' | 'blue' | 'purple' | 'teal' | 'gray'

export interface CouponCardItem {
  id: number
  title: string
  typeLabel: string
  scopeLabel: string
  amount: number
  threshold: number
  startTime?: string
  endTime?: string
  stockText?: string
  receiveText?: string
  statusText: string
  actionText: string
  actionDisabled: boolean
  state: CouponCardState
  tone: CouponCardTone
  rules: string[]
}

export interface CouponTabItem<T extends string = string> {
  key: T
  label: string
  count: number
}
