/**
 * 证件号码验证工具
 * 支持身份证、护照、驾驶证等证件的格式验证
 */

/**
 * 验证证件号码
 * @param {string} idType - 证件类型：id_card(身份证)、passport(护照)、drivers_license(驾驶证)、other(其他)
 * @param {string} idNumber - 证件号码
 * @returns {Object} - { valid: boolean, message: string }
 */
export const validateIdNumber = (idType, idNumber) => {
  if (!idNumber || !idNumber.trim()) {
    return { valid: true, message: '' }
  }

  const trimmedNumber = idNumber.trim()

  switch (idType) {
    case 'id_card':
      return validateIdCard(trimmedNumber)

    case 'passport':
      if (!/^[A-Za-z0-9]{6,20}$/.test(trimmedNumber)) {
        return { valid: false, message: '护照号码格式不正确（应为6-20位字母或数字）' }
      }
      return { valid: true, message: '' }

    case 'drivers_license':
      if (!/^[A-Za-z0-9]{8,20}$/.test(trimmedNumber)) {
        return { valid: false, message: '驾驶证号码格式不正确（应为8-20位字母或数字）' }
      }
      return { valid: true, message: '' }

    case 'other':
    default:
      if (trimmedNumber.length < 4 || trimmedNumber.length > 50) {
        return { valid: false, message: '证件号码长度应在4-50位之间' }
      }
      return { valid: true, message: '' }
  }
}

/**
 * 验证18位身份证号码
 * 规则：
 * 1. 格式：17位数字 + 1位数字或X
 * 2. 校验码验证：根据前17位计算第18位校验码
 */
const validateIdCard = (idNumber) => {
  // 格式验证：17位数字 + 1位数字或X
  const idRegex = /^\d{17}[\dXx]$/
  if (!idRegex.test(idNumber)) {
    return { valid: false, message: '身份证号码格式不正确（应为18位，前17位为数字，最后一位为数字或X）' }
  }

  // 校验码验证
  const factors = [7, 9, 10, 5, 8, 4, 2, 1, 6, 3, 7, 9, 10, 5, 8, 4, 2]
  const checkCodes = ['1', '0', 'X', '9', '8', '7', '6', '5', '4', '3', '2']

  let sum = 0
  for (let i = 0; i < 17; i++) {
    sum += parseInt(idNumber[i]) * factors[i]
  }
  const remainder = sum % 11
  const checkCode = checkCodes[remainder]

  if (idNumber[17].toUpperCase() !== checkCode) {
    return { valid: false, message: '身份证号码校验码不正确，请检查是否输入错误' }
  }

  return { valid: true, message: '' }
}

/**
 * 验证手机号
 * @param {string} phone - 手机号
 * @returns {Object} - { valid: boolean, message: string }
 */
export const validatePhone = (phone) => {
  if (!phone || !phone.trim()) {
    return { valid: true, message: '' }
  }

  const cleanedPhone = phone.replace(/[-\s]/g, '')
  if (!/^[+]?[1-9]\d{1,14}$/.test(cleanedPhone)) {
    return { valid: false, message: '手机号格式不正确' }
  }
  return { valid: true, message: '' }
}

/**
 * 验证邮箱
 * @param {string} email - 邮箱
 * @returns {Object} - { valid: boolean, message: string }
 */
export const validateEmail = (email) => {
  if (!email || !email.trim()) {
    return { valid: true, message: '' }
  }

  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
  if (!emailRegex.test(email)) {
    return { valid: false, message: '邮箱格式不正确' }
  }
  return { valid: true, message: '' }
}
