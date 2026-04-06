import { Routes, Route, Navigate } from 'react-router-dom'
import Header from './components/Header/Header'
import MeasurementPage from './pages/MeasurementPage'
import HistoryPage from './pages/HistoryPage'

export default function App() {
  return (
    <>
      <Header />
      <main>
        <Routes>
          {/* Default → length */}
          <Route path="/"            element={<Navigate to="/length" replace />} />
          <Route path="/length"      element={<MeasurementPage key="length"      category="length" />} />
          <Route path="/weight"      element={<MeasurementPage key="weight"      category="weight" />} />
          <Route path="/temperature" element={<MeasurementPage key="temperature" category="temperature" />} />
          <Route path="/volume"      element={<MeasurementPage key="volume"      category="volume" />} />
          <Route path="/history"     element={<HistoryPage />} />
          {/* Fallback */}
          <Route path="*"            element={<Navigate to="/length" replace />} />
        </Routes>
      </main>
    </>
  )
}
