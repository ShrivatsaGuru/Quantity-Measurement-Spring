import styles from './ActionSelector.module.scss'

export default function ActionSelector({ actions, active, onChange }) {
  return (
    <div className={styles.wrapper}>
      {actions.map(action => (
        <button
          key={action.key}
          className={`${styles.btn} ${active === action.key ? styles.active : ''}`}
          onClick={() => onChange(action.key)}
        >
          {action.label}
        </button>
      ))}
    </div>
  )
}
