import styles from './ResultCard.module.scss'

/** Displays the API response — adapts to comparison (true/false) vs numeric result */
export default function ResultCard({ result, action }) {
  const isCompare = action === 'compare'
  const isEqual   = result?.resultString === 'true'

  return (
    <div className={`${styles.card} ${result?.error ? styles.cardError : styles.cardSuccess}`}>
      <div className={styles.header}>
        <span className={styles.label}>Result</span>
        {result?.error
          ? <span className="badge badge--error">⚠ Error</span>
          : <span className="badge badge--success">✓ Success</span>
        }
      </div>

      {result?.error ? (
        <p className={styles.errorMsg}>{result.errorMessage}</p>
      ) : (
        <>
          {/* Human-readable expression from backend */}
          {result?.resultString && !isCompare && (
            <p className={styles.expression}>{result.resultString}</p>
          )}

          {/* Compare: big true/false badge */}
          {isCompare && (
            <div className={`${styles.compareBadge} ${isEqual ? styles.equal : styles.notEqual}`}>
              {isEqual ? '✓ Equal' : '✗ Not Equal'}
            </div>
          )}

          {/* Numeric result */}
          {!isCompare && result?.resultValue !== undefined && (
            <div className={styles.numericResult}>
              <span className={styles.resultValue}>
                {Number(result.resultValue).toLocaleString(undefined, { maximumFractionDigits: 6 })}
              </span>
              {result?.resultUnit && (
                <span className={styles.resultUnit}>{result.resultUnit.replace(/_/g, ' ')}</span>
              )}
            </div>
          )}

          {/* Metadata row */}
          <div className={styles.meta}>
            <span className={styles.metaItem}>
              <b>Operation:</b> {result?.operation}
            </span>
            <span className={styles.metaItem}>
              <b>Type:</b> {result?.thisMeasurementType}
            </span>
          </div>
        </>
      )}
    </div>
  )
}
