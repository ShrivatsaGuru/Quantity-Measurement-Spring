import axios from 'axios'

const BASE = '/api/v1/quantities'

// ── POST operations ───────────────────────────────────────────────────────────

/** Build the standard request body */
const body = (thisQty, thatQty, targetQty = null) => ({
  thisQuantityDTO: thisQty,
  thatQuantityDTO: thatQty,
  ...(targetQty ? { targetQuantityDTO: targetQty } : {}),
})

export const compare  = (a, b)         => axios.post(`${BASE}/compare`,                 body(a, b))
export const convert  = (a, b)         => axios.post(`${BASE}/convert`,                 body(a, b))
export const add      = (a, b, t)      => axios.post(t ? `${BASE}/add-with-target-unit` : `${BASE}/add`,      body(a, b, t))
export const subtract = (a, b, t)      => axios.post(t ? `${BASE}/subtract-with-target-unit` : `${BASE}/subtract`, body(a, b, t))
export const multiply = (a, b)         => axios.post(`${BASE}/multiply`,                body(a, b))
export const divide   = (a, b)         => axios.post(`${BASE}/divide`,                  body(a, b))

// ── GET history ───────────────────────────────────────────────────────────────

export const getHistoryByOperation = (op)   => axios.get(`${BASE}/history/operation/${op}`)
export const getHistoryByType      = (type) => axios.get(`${BASE}/history/type/${type}`)
export const getErroredHistory     = ()     => axios.get(`${BASE}/history/errored`)
export const getOperationCount     = (op)   => axios.get(`${BASE}/count/${op}`)
