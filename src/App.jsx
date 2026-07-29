import { BrowserRouter, Routes, Route } from "react-router-dom"
import Home from "./pages/Home"
import VillaDetails from "./pages/VillaDetails"
import Login from "./pages/Login"
import Admin from "./pages/Admin"

function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<Home />} />
        <Route path="/villa/:id" element={<VillaDetails />} />
        <Route path="/login" element={<Login />} />
        <Route path="/admin" element={<Admin />} />
      </Routes>
    </BrowserRouter>
  )
}

export default App