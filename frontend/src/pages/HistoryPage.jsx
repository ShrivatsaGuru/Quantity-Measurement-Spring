import { useState, useEffect } from 'react'
import * as api from '../api/quantityApi'
import styles from './HistoryPage.module.scss'

const OPERATIONS = ['ADD', 'SUBTRACT', 'MULTIPLY', 'DIVIDE', 'COMPARE', 'CONVERT']
const TYPES      = ['LengthUnit', 'WeightUnit', 'TemperatureUnit', 'VolumeUnit']

export default function HistoryPage() {
  const [tab, setTab]         = useState('operation')  // operation | type | errored | count
  const [filter, setFilter]   = useState('ADD')
  const [typeFilter, setTypeFilter] = useState('LengthUnit')
  const [records, setRecords] = useState([])
  const [count, setCount]     = useState(null)
  const [loading, setLoading] = useState(false)
  const [error, setError]     = useState(null)

  const fetchData = async () => {
    setLoading(true); setError(null); setCount(null)
    try {
      let res
      if (tab === 'operation') res = await api.getHistoryByOperation(filter)
      else if (tab === 'type') res = await api.getHistoryByType(typeFilter)
      else if (tab === 'errored') res = await api.getErroredHistory()
      else if (tab === 'count') {
        const r = await api.getOperationCount(filter)
        setCount(r.data)
        setRecords([])
        setLoading(false)
        return
      }
      setRecords(res.data ?? [])
    } catch (e) {
      setError(e.response?.data?.message ?? e.message)
      setRecords([])
    } finally {
      setLoading(false)
    }
  }

  // Auto-fetch when tab/filter changes
  useEffect(() => { fetchData() }, [tab, filter, typeFilter]) // eslint-disable-line

  return (
    <div className={styles.page}>
      <div className="container">
        <div className={styles.pageTitle}>
          <span>📋</span>
          <h1>Operation History</h1>
        </div>

        {/* Tab bar */}
        <div className={styles.tabs}>
          {[
            { key: 'operation', label: '⚙ By Operation' },
            { key: 'type',      label: '📐 By Type' },
            { key: 'errored',   label: '⚠ Errors' },
            { key: 'count',     label: '🔢 Count' },
          ].map(t => (
            <button
              key={t.key}
              className={`${styles.tab} ${tab === t.key ? styles.tabActive : ''}`}
              onClick={() => setTab(t.key)}
            >
              {t.label}
            </button>
          ))}
        </div>

        {/* Filters */}
        <div className={styles.filterBar}>
          {(tab === 'operation' || tab === 'count') && (
            <div className={styles.filterGroup}>
              <label>Operation</label>
              <select value={filter} onChange={e => setFilter(e.target.value)}>
                {OPERATIONS.map(op => <option key={op} value={op}>{op}</option>)}
              </select>
            </div>
          )}
          {tab === 'type' && (
            <div className={styles.filterGroup}>
              <label>Measurement Type</label>
              <select value={typeFilter} onChange={e => setTypeFilter(e.target.value)}>
                {TYPES.map(t => <option key={t} value={t}>{t}</option>)}
              </select>
            </div>
          )}
          <button className={styles.refreshBtn} onClick={fetchData} disabled={loading}>
            {loading ? '…' : '↻ Refresh'}
          </button>
        </div>

        {/* Count display */}
        {tab === 'count' && count !== null && (
          <div className={styles.countCard}>
            <span className={styles.countNum}>{count}</span>
            <span className={styles.countLabel}>
              successful <strong>{filter}</strong> operations
            </span>
          </div>
        )}

        {/* Error */}
        {error && (
          <div className={styles.errorBox}>⚠️ {error}</div>
        )}

        {/* Table */}
        {tab !== 'count' && !loading && records.length === 0 && !error && (
          <div className={styles.empty}>No records found.</div>
        )}

        {records.length > 0 && (
          <div className={styles.tableWrap}>
            <table className={styles.table}>
              <thead>
                <tr>
                  <th>Operation</th>
                  <th>From</th>
                  <th>To</th>
                  <th>Result</th>
                  <th>Status</th>
                </tr>
              </thead>
              <tbody>
                {records.map((r, i) => (
                  <tr key={i} className={r.error ? styles.rowError : ''}>
                    <td>
                      <span className={styles.opBadge}>{r.operation}</span>
                    </td>
                    <td>
                      <span className={styles.val}>{r.thisValue}</span>
                      <span className={styles.unit}>{r.thisUnit?.replace(/_/g,' ')}</span>
                      <span className={styles.type}>{r.thisMeasurementType}</span>
                    </td>
                    <td>
                      <span className={styles.val}>{r.thatValue}</span>
                      <span className={styles.unit}>{r.thatUnit?.replace(/_/g,' ')}</span>
                    </td>
                    <td className={styles.resultCol}>
                      {r.error
                        ? <span className={styles.errMsg}>{r.errorMessage}</span>
                        : <span className={styles.resultStr}>{r.resultString || `${r.resultValue} ${r.resultUnit ?? ''}`}</span>
                      }
                    </td>
                    <td>
                      {r.error
                        ? <span className="badge badge--error">Error</span>
                        : <span className="badge badge--success">OK</span>
                      }
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </div>
  )
}
