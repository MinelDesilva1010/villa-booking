import { useState } from "react"
import { Link, useNavigate } from "react-router-dom"

function Login() {
  const [isSignup, setIsSignup] = useState(false)
  const [name, setName] = useState("")
  const [email, setEmail] = useState("")
  const [password, setPassword] = useState("")
  const [error, setError] = useState("")
  const [loading, setLoading] = useState(false)

  const navigate = useNavigate()

  async function handleSubmit(e) {
    e.preventDefault()
    setError("")
    setLoading(true)

    const url = isSignup
      ? "https://villa-backend-1gzn.onrender.com/api/signup"
      : "https://villa-backend-1gzn.onrender.com/api/login"

    const body = isSignup
      ? { name, email, password }
      : { email, password }

    try {
      const response = await fetch(url, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(body),
      })

      const data = await response.json()

      if (data.success) {
        // Save token and name to localStorage
        localStorage.setItem("token", data.token)
        localStorage.setItem("userName", data.name)
        // Redirect to homepage
        navigate("/")
      } else {
        setError(data.message)
      }
    } catch (err) {
      setError("Something went wrong. Please try again.")
    }

    setLoading(false)
  }

  return (
    <div>
      <nav className="navbar">
        <Link to="/" style={{ textDecoration: "none" }}>
          <div className="logo">Ceylon<span>Villas</span></div>
        </Link>
        <div className="nav-links">
          <Link to="/">Home</Link>
        </div>
      </nav>

      <div className="section" style={{ maxWidth: "380px", margin: "2rem auto" }}>
        <h2 className="section-title">{isSignup ? "Create account" : "Sign in"}</h2>
        <p className="section-sub">
          {isSignup ? "Join CeylonVillas today" : "Welcome back to CeylonVillas"}
        </p>

        <form onSubmit={handleSubmit}>
          {isSignup && (
            <div className="search-field" style={{ marginBottom: "1rem" }}>
              <label>Full name</label>
              <input
                type="text"
                placeholder="Your name"
                value={name}
                onChange={(e) => setName(e.target.value)}
                required
              />
            </div>
          )}

          <div className="search-field" style={{ marginBottom: "1rem" }}>
            <label>Email</label>
            <input
              type="email"
              placeholder="you@example.com"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              required
            />
          </div>

          <div className="search-field" style={{ marginBottom: "1rem" }}>
            <label>Password</label>
            <input
              type="password"
              placeholder="••••••••"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              required
            />
          </div>

          {error && (
            <p style={{ color: "red", fontSize: "13px", marginBottom: "1rem" }}>{error}</p>
          )}

          <button
            type="submit"
            className="search-btn"
            style={{ width: "100%", marginBottom: "1rem" }}
            disabled={loading}
          >
            {loading ? "Please wait..." : isSignup ? "Create account" : "Sign in"}
          </button>
        </form>

        <p style={{ fontSize: "14px", textAlign: "center", color: "#888" }}>
          {isSignup ? "Already have an account?" : "Don't have an account?"}{" "}
          <span
            onClick={() => { setIsSignup(!isSignup); setError("") }}
            style={{ color: "#1D9E75", cursor: "pointer", fontWeight: "600" }}
          >
            {isSignup ? "Sign in" : "Sign up"}
          </span>
        </p>
      </div>
    </div>
  )
}

export default Login