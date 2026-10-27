type TageType = | 'primary' | 'success' | 'warning' | 'danger' | 'info'

interface TagDisplay{
    label:string
    type:TageType
}

const statusMap:Record<string,TagDisplay> = {
    OPEN: {
        label: '待处理',
        type: 'warning'
    },
    IN_PROGRESS: {
        label: '处理中',
        type: 'primary'
    },
    RESOLVED: {
        label: '已解决',
        type: 'success'
    },
    CLOSED: {
        label: '已关闭',
        type: 'info'
    },
    CANCELLED: {
        label: '已取消',
        type: 'danger'
    }
}

const priorityMap: Record<string, TagDisplay> = {
  LOW: {
    label: '低',
    type: 'info'
  },
  MEDIUM: {
    label: '普通',
    type: 'primary'
  },
  HIGH: {
    label: '高',
    type: 'warning'
  },
  URGENT: {
    label: '紧急',
    type: 'danger'
  }
}

export function getStatusDisplay(status:string):TagDisplay{
    return statusMap[status] ?? {
        label:status,
        type:'info'
    }
}

export function getPriorityDisplay(priority:string):TagDisplay{
    return priorityMap[priority] ?? {
        label:priority,
        type:'info'
    }
}

export function formatDateTime(
  value: string | null | undefined
): string {
  if (!value) {
    return '-'
  }

  const [datePart, timePart] = value
    .replace('T', ' ')
    .split(' ')

  const date = datePart.replace(/-/g, '/')
  const time = (timePart ?? '').slice(0, 8)

  return time ? `${date} ${time}` : date
}
