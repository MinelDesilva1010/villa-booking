import { useState, useEffect } from "react"
import { Link } from "react-router-dom"

function Home() {
  const [villas, setVillas] = useState([])
  const [loading, setLoading] = useState(true)
  const [userName, setUserName] = useState(localStorage.getItem("userName"))
  const [search, setSearch] = useState("")
  const [activeSearch, setActiveSearch] = useState("")

  function handleSignOut() {
    localStorage.removeItem("token")
    localStorage.removeItem("userName")
    setUserName(null)
  }

  useEffect(() => {
    fetch("https://villa-backend-1gzn.onrender.com/api/villas")
      .then((res) => res.json())
      .then((data) => {
        setVillas(data)
        setLoading(false)
      })
  }, [])

  // This goes OUTSIDE the return, INSIDE the function
  const filteredVillas = villas.filter((villa) =>
  villa.name.toLowerCase().includes(activeSearch.toLowerCase()) ||
  villa.location.toLowerCase().includes(activeSearch.toLowerCase())
)

  return (
    <div>
      <nav className="navbar">
        <div className="logo">Ceylon<span> Villas</span></div>
        <div className="nav-links">
          <a href="#">Villas</a>
          <a href="#">Locations</a>
          <a href="#">About</a>
          {userName ? (
            <div style={{ display: "flex", alignItems: "center", gap: "12px" }}>
              <span style={{ fontSize: "14px", color: "#555" }}>👋 {userName}</span>
              <button className="nav-btn" onClick={handleSignOut}>Sign out</button>
            </div>
          ) : (
            <Link to="/login"><button className="nav-btn">Sign in</button></Link>
          )}
        </div>
      </nav>

      <div className="hero">
        <h1>Find your perfect villa getaway</h1>
        <p>Handpicked luxury villas across the most beautiful destinations</p>
        <div className="search-box">
          <div className="search-field">
            <label>Destination</label>
            <input
              type="text"
              placeholder="Where to?"
              value={search}
              onChange={(e) => setSearch(e.target.value)}
            />
          </div>
          <div className="search-field">
            <label>Check in</label>
            <input type="date" />
          </div>
          <div className="search-field">
            <label>Check out</label>
            <input type="date" />
          </div>
          <div className="search-field">
            <label>Guests</label>
            <select>
              <option>2</option>
              <option>4</option>
              <option>6</option>
              <option>8+</option>
            </select>
          </div>
          <button className="search-btn" onClick={() => setActiveSearch(search)}>Search</button>
        </div>
      </div>

      <div className="section">
        <h2 className="section-title">Featured villas</h2>
        <p className="section-sub">Our most loved stays this season</p>

        {loading ? (
          <p>Loading villas...</p>
        ) : (
          <div className="villas-grid">
            {filteredVillas.length === 0 && (
              <p style={{ color: "#888", marginTop: "1rem" }}>No villas found for "{search}"</p>
            )}
            {filteredVillas.map((villa) => (
              <Link to={`/villa/${villa._id}`} key={villa._id} style={{ textDecoration: "none" }}>
                <div className="villa-card">
                  <div className="villa-img" style={{ background: villa.color }}>
                    {villa.image
                      ? <img src={villa.image} alt={villa.name} style={{ width: "100%", height: "100%", objectFit: "cover" }} />
                      : villa.emoji
                    }
                  </div>
                  <div className="villa-body">
                    <div className="villa-name">{villa.name}</div>
                    <div className="villa-loc">📍 {villa.location}</div>
                    <div className="villa-meta">
                      <div className="villa-price">${villa.price} <span>/ night</span></div>
                      <div className="villa-stars">{villa.rating}</div>
                    </div>
                  </div>
                </div>
              </Link>
            ))}
          </div>
        )}
      </div>
    </div>
  )
}

export default Home