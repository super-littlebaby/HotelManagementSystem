const pad = (n) => String(n).padStart(2, '0')

export function formatDate(val) {
  if (!val) return '-'
  if (typeof val === 'string') {
    return val.replace('T', ' ').substring(0, 10)
  }
  const d = new Date(val)
  if (isNaN(d.getTime())) return val
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`
}

export function formatDateTime(val) {
  if (!val) return '-'
  if (typeof val === 'string') {
    return val.replace('T', ' ').replace(/\.\d+$/, '')
  }
  const d = new Date(val)
  if (isNaN(d.getTime())) return val
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}:${pad(d.getSeconds())}`
}
