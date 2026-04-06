import { NavLink } from 'react-router-dom'
import { CATEGORIES } from '../../config/categories'
import styles from './Header.module.scss'

export default function Header() {
  return (
    <header className={styles.header}>
      <div className={`container ${styles.inner}`}>
        {/* Brand */}
        <div className={styles.brand}>
          <span className={styles.brandIcon}>⚡</span>
          <span className={styles.brandText}>Quantity Measurement</span>
        </div>

        {/* Category nav */}
        <nav className={styles.nav}>
          {CATEGORIES.map(cat => (
            <NavLink
              key={cat.key}
              to={`/${cat.key}`}
              className={({ isActive }) =>
                `${styles.navItem} ${isActive ? styles.active : ''}`
              }
            >
              <span className={styles.navIcon}>{cat.icon}</span>
              <span>{cat.label}</span>
            </NavLink>
          ))}
          <NavLink
            to="/history"
            className={({ isActive }) =>
              `${styles.navItem} ${isActive ? styles.active : ''}`
            }
          >
            <span className={styles.navIcon}>📋</span>
            <span>History</span>
          </NavLink>
        </nav>
      </div>
    </header>
  )
}
