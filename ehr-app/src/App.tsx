import './App.css'
import LoginPage from './pages/LoginPage'
import { AuthProvider } from './contexts/AuthProvider'
import { BrowserRouter, Route, Routes } from 'react-router-dom'
import HomePage from './pages/HomePage'
import AuthenticatedLayout from './layouts/AuthenticatedLayout'

function App() {

  return (
    <BrowserRouter>
      <AuthProvider>
        <Routes>
          <Route path="/login" element={<LoginPage />} />

          <Route element={<AuthenticatedLayout />}>
            <Route path="/home" element={<HomePage />} />
          </Route>
        </Routes>
      </AuthProvider>
    </BrowserRouter>
  )
}

export default App
