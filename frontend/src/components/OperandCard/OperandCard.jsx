import styles from './OperandCard.module.scss'

/**
 * A card containing a numeric input and unit dropdown.
 * unitOnly: hide number input (used for the TO card in convert mode).
 */
export default function OperandCard({ label, value, unit, units, onChange, unitOnly = false }) {
  const handleValue = (e) => onChange(e.target.value, unit)
  const handleUnit  = (e) => onChange(value, e.target.value)

  return (
    <div className={styles.card}>
      <span className={styles.label}>{label}</span>

      {!unitOnly && (
        <input
          className={styles.input}
          type="number"
          placeholder="0"
          value={value}
          onChange={handleValue}
          step="any"
        />
      )}

      <div className={styles.selectWrap}>
        <select
          className={styles.select}
          value={unit}
          onChange={handleUnit}
        >
          {units.map(u => (
            <option key={u} value={u}>{u.replace(/_/g, ' ')}</option>
          ))}
        </select>
        <span className={styles.chevron}>▾</span>
      </div>
    </div>
  )
}
