import { useState, useCallback } from 'react'
import { CATEGORIES, ACTIONS, OPERATORS, qty } from '../config/categories'
import * as api from '../api/quantityApi'
import ActionSelector from '../components/ActionSelector/ActionSelector'
import OperandCard from '../components/OperandCard/OperandCard'
import ResultCard from '../components/ResultCard/ResultCard'
import styles from './MeasurementPage.module.scss'

export default function MeasurementPage({ category }) {
  const cat = CATEGORIES.find(c => c.key === category)

  // ── State ─────────────────────────────────────────────────────────────────
  const [action, setAction]     = useState('compare')    // compare | convert | arithmetic
  const [operator, setOperator] = useState('add')        // add | subtract | multiply | divide
  const [thisVal, setThisVal]   = useState('')
  const [thisUnit, setThisUnit] = useState(cat.units[0])
  const [thatVal, setThatVal]   = useState('')
  const [thatUnit, setThatUnit] = useState(cat.units[1] ?? cat.units[0])
  const [targetUnit, setTargetUnit] = useState(cat.units[0])
  const [result, setResult]     = useState(null)
  const [loading, setLoading]   = useState(false)
  const [error, setError]       = useState(null)

  // Reset result whenever anything changes
  const resetResult = () => { setResult(null); setError(null) }

  // Re-initialise units when category changes (handled by key in App)
  // Action change
  const handleAction = (a) => { setAction(a); resetResult() }
  const handleOperator = (o) => { setOperator(o); resetResult() }

  // ── Submit ────────────────────────────────────────────────────────────────
  const submit = useCallback(async () => {
    if (thisVal === '') return
    if (action !== 'convert' && thatVal === '') return
    setLoading(true); setError(null); setResult(null)
    try {
      const a = qty(thisVal, thisUnit, cat.type)
      const b = qty(thatVal, thatUnit, cat.type)
      const t = qty(0, targetUnit, cat.type)

      let res
      if (action === 'compare')    res = await api.compare(a, b)
      else if (action === 'convert') res = await api.convert(a, b)
      else {
        // arithmetic
        const needsTarget = operator === 'add' || operator === 'subtract'
        if (operator === 'add')      res = await api.add(a, b, needsTarget ? t : undefined)
        else if (operator === 'subtract') res = await api.subtract(a, b, needsTarget ? t : undefined)
        else if (operator === 'multiply') res = await api.multiply(a, b)
        else if (operator === 'divide')   res = await api.divide(a, b)
      }
      setResult(res.data)
    } catch (err) {
      setError(err.response?.data?.message ?? err.message ?? 'Request failed')
    } finally {
      setLoading(false)
    }
  }, [action, operator, thisVal, thisUnit, thatVal, thatUnit, targetUnit, cat])

  // ── Labels per action ─────────────────────────────────────────────────────
  const label1 = action === 'convert' ? 'FROM' : action === 'compare' ? 'VALUE 1' : 'VALUE 1'
  const label2 = action === 'convert' ? 'TO UNIT' : action === 'compare' ? 'VALUE 2' : 'VALUE 2'

  return (
    <div className={styles.page}>
      <div className="container">

        {/* Page title */}
        <div className={styles.pageTitle}>
          <span className={styles.icon}>{cat.icon}</span>
          <h1>{cat.label} Measurement</h1>
        </div>

        {/* Action selector — Compare / Convert / Arithmetic */}
        <ActionSelector
          actions={ACTIONS}
          active={action}
          onChange={handleAction}
        />

        {/* Operator picker (only for arithmetic) */}
        {action === 'arithmetic' && (
          <div className={styles.operatorRow}>
            {OPERATORS.map(op => (
              <button
                key={op.key}
                className={`${styles.opBtn} ${operator === op.key ? styles.opActive : ''}`}
                onClick={() => handleOperator(op.key)}
                title={op.label}
              >
                <span className={styles.opSymbol}>{op.symbol}</span>
                <span>{op.label}</span>
              </button>
            ))}
          </div>
        )}

        {/* Input area */}
        <div className={styles.inputArea}>
          {/* First operand */}
          <OperandCard
            label={label1}
            value={thisVal}
            unit={thisUnit}
            units={cat.units}
            onChange={(v, u) => { setThisVal(v); setThisUnit(u); resetResult() }}
            hideValue={action === 'convert'}
          />

          {/* Centre badge — operator symbol or comparison icon */}
          <div className={styles.centre}>
            {action === 'compare'    && <span className={styles.centreIcon}>⇌</span>}
            {action === 'convert'    && <span className={styles.centreIcon}>→</span>}
            {action === 'arithmetic' && (
              <span className={styles.centreSymbol}>
                {OPERATORS.find(o => o.key === operator)?.symbol}
              </span>
            )}
          </div>

          {/* Second operand */}
          <OperandCard
            label={label2}
            value={action === 'convert' ? '' : thatVal}
            unit={thatUnit}
            units={cat.units}
            onChange={(v, u) => { setThatVal(v); setThatUnit(u); resetResult() }}
            hideValue={action === 'convert'}   // for convert, only the unit matters
            unitOnly={action === 'convert'}
          />
        </div>

        {/* Target unit (for arithmetic add/subtract) */}
        {action === 'arithmetic' && (operator === 'add' || operator === 'subtract') && (
          <div className={styles.targetRow}>
            <label className={styles.targetLabel}>Result Unit</label>
            <select
              className={styles.targetSelect}
              value={targetUnit}
              onChange={e => { setTargetUnit(e.target.value); resetResult() }}
            >
              {cat.units.map(u => <option key={u} value={u}>{u}</option>)}
            </select>
          </div>
        )}

        {/* Submit */}
        <button
          className={styles.calcBtn}
          onClick={submit}
          disabled={loading || thisVal === '' || (action !== 'convert' && thatVal === '')}
        >
          {loading ? <span className={styles.spinner} /> : null}
          {loading ? 'Calculating…' : 'Calculate'}
        </button>

        {/* Error */}
        {error && (
          <div className={styles.errorBox}>
            <span>⚠️</span> {error}
          </div>
        )}

        {/* Result */}
        {result && <ResultCard result={result} action={action} />}

      </div>
    </div>
  )
}
