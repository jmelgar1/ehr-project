import './App.css'
import LoginPage from './pages/LoginPage'
import { AuthProvider } from './contexts/AuthContext'
import { BrowserRouter, Route, Routes } from 'react-router-dom'
import HomePage from './pages/HomePage'

function App() {

  return (
    <BrowserRouter>
      <AuthProvider>
        <Routes>
          <Route path="/login" element={<LoginPage />} />
          <Route path="/home" element={<HomePage />} />
        </Routes>
      </AuthProvider>
    </BrowserRouter>
  )
}

export default App
