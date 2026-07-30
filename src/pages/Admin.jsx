import { useState, useEffect } from "react"
import { Link } from "react-router-dom"

const ADMIN_PASSWORD = "admin123"

function Admin() {
  const [authed, setAuthed] = useState(false)
  const [passwordInput, setPasswordInput] = useState("")
  const [error, setError] = useState("")
  const [tab, setTab] = useState("villas")

  // Villas state
  const [villas, setVillas] = useState([])
  const [editingVilla, setEditingVilla] = useState(null)
  const [villaForm, setVillaForm] = useState({ name: "", location: "", price: "", color: "#E1F5EE", image: "", desc: "", rating: "★★★★★" })

  // Bookings state
  const [bookings, setBookings] = useState([])

  const [packages, setPackages] = useState([])
const [selectedVillaId, setSelectedVillaId] = useState("")
const [packageForm, setPackageForm] = useState({ name: "", desc: "", guests: 2, price: "" })
const [editingPackage, setEditingPackage] = useState(null)

  useEffect(() => {
  if (authed) {
    fetchVillas()
    fetchBookings()
  }
}, [authed])

useEffect(() => {
  if (selectedVillaId) {
    fetchPackages(selectedVillaId)
  }
}, [selectedVillaId])    

  function handleLogin(e) {
    e.preventDefault()
    if (passwordInput === ADMIN_PASSWORD) {
      setAuthed(true)
    } else {
      setError("Wrong password!")
    }
  }

  async function fetchVillas() {
    const res = await fetch("https://villa-backend-1gzn.onrender.com/api/villas")
    const data = await res.json()
    setVillas(data)
  }

  async function fetchBookings() {
    const res = await fetch("https://villa-backend-1gzn.onrender.com/api/bookings")
    const data = await res.json()
    setBookings(data)
  }

  async function fetchPackages(villaId) {
  const res = await fetch(`https://villa-backend-1gzn.onrender.com/api/packages/${villaId}`)
  const data = await res.json()
  setPackages(data)
}

async function handlePackageSubmit(e) {
  e.preventDefault()
  const url = editingPackage
    ? `https://villa-backend-1gzn.onrender.com/api/packages/${editingPackage._id}`
    : "https://villa-backend-1gzn.onrender.com/api/packages"
  const method = editingPackage ? "PUT" : "POST"

  const res = await fetch(url, {
    method,
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ ...packageForm, villaId: selectedVillaId, price: Number(packageForm.price), guests: Number(packageForm.guests) }),
  })
  const data = await res.json()
  if (data.success) {
    fetchPackages(selectedVillaId)
    setPackageForm({ name: "", desc: "", guests: 2, price: "" })
    setEditingPackage(null)
  }
}

async function handleDeletePackage(id) {
  if (!window.confirm("Delete this package?")) return
  await fetch(`https://villa-backend-1gzn.onrender.com/api/packages/${id}`, { method: "DELETE" })
  fetchPackages(selectedVillaId)
}

function handleEditPackage(pkg) {
  setEditingPackage(pkg)
  setPackageForm({ name: pkg.name, desc: pkg.desc, guests: pkg.guests, price: pkg.price })
}

  async function handleVillaSubmit(e) {
    e.preventDefault()
    const url = editingVilla
      ? `https://villa-backend-1gzn.onrender.com/api/villas/${editingVilla._id}`
      : "https://villa-backend-1gzn.onrender.com/api/villas"
    const method = editingVilla ? "PUT" : "POST"

    const res = await fetch(url, {
      method,
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ ...villaForm, price: Number(villaForm.price) }),
    })
    const data = await res.json()
    if (data.success) {
      fetchVillas()
      setVillaForm({ name: "", location: "", price: "", color: "#E1F5EE", image: "", desc: "", rating: "★★★★★" })
      setEditingVilla(null)
    }
  }

  async function handleDeleteVilla(id) {
    if (!window.confirm("Are you sure you want to delete this villa?")) return
    await fetch(`https://villa-backend-1gzn.onrender.com/api/villas/${id}`, { method: "DELETE" })
    fetchVillas()
  }

  function handleEditVilla(villa) {
    setEditingVilla(villa)
    setVillaForm({
      name: villa.name,
      location: villa.location,
      price: villa.price,
      color: villa.color,
      image: villa.image,
      desc: villa.desc,
      rating: villa.rating,
    })
  }

  if (!authed) {
    return (
      <div>
        <nav className="navbar">
          <Link to="/" style={{ textDecoration: "none" }}><div className="logo">Ceylon<span>Villas</span></div></Link>
        </nav>
        <div className="section" style={{ maxWidth: "360px", margin: "2rem auto" }}>
          <h2 className="section-title">Admin Login</h2>
          <p className="section-sub">Enter your admin password to continue</p>
          <form onSubmit={handleLogin}>
            <div className="search-field" style={{ marginBottom: "1rem" }}>
              <label>Password</label>
              <input
                type="password"
                placeholder="••••••••"
                value={passwordInput}
                onChange={(e) => setPasswordInput(e.target.value)}
                required
              />
            </div>
            {error && <p style={{ color: "red", fontSize: "13px", marginBottom: "1rem" }}>{error}</p>}
            <button type="submit" className="search-btn" style={{ width: "100%" }}>Enter Admin Panel</button>
          </form>
        </div>
      </div>
    )
  }

  return (
    <div>
      <nav className="navbar">
        <Link to="/" style={{ textDecoration: "none" }}><div className="logo">Ceylon<span>Villas</span></div></Link>
        <div className="nav-links">
          <Link to="/">← Back to site</Link>
        </div>
      </nav>

      <div className="section">
        <h2 className="section-title">Admin Panel</h2>

        {/* Tabs */}
        <div style={{ display: "flex", gap: "12px", marginBottom: "2rem", marginTop: "1rem" }}>
          <button
            className="search-btn"
            onClick={() => setTab("villas")}
            style={{ background: tab === "villas" ? "#0F6E56" : "#ccc" }}
          >
            🏡 Villas
          </button>
          <button
            className="search-btn"
            onClick={() => setTab("bookings")}
            style={{ background: tab === "bookings" ? "#0F6E56" : "#ccc" }}
          >
            📋 Bookings
          </button>

          <button
  className="search-btn"
  onClick={() => setTab("packages")}
  style={{ background: tab === "packages" ? "#0F6E56" : "#ccc" }}
>
  🏨 Packages
</button>
          
          
        </div>

        {/* VILLAS TAB */}
        {tab === "villas" && (
          <div>
            <h3 style={{ marginBottom: "1rem" }}>{editingVilla ? "✏️ Edit Villa" : "➕ Add New Villa"}</h3>
            <form onSubmit={handleVillaSubmit} style={{ maxWidth: "500px", marginBottom: "2rem" }}>
              {[
                { label: "Villa Name", key: "name", type: "text", placeholder: "e.g. Sunset Villa" },
                { label: "Location", key: "location", type: "text", placeholder: "e.g. Galle, Sri Lanka" },
                { label: "Price per night ($)", key: "price", type: "number", placeholder: "e.g. 350" },
            
                { label: "Description", key: "desc", type: "text", placeholder: "Short description..." },
              ].map((field) => (
                <div className="search-field" key={field.key} style={{ marginBottom: "1rem" }}>
                  <label>{field.label}</label>
                  <input
                    type={field.type}
                    placeholder={field.placeholder}
                    value={villaForm[field.key]}
                    onChange={(e) => setVillaForm({ ...villaForm, [field.key]: e.target.value })}
                    required
                  />
                </div>
              ))}

              <div className="search-field" style={{ marginBottom: "1rem" }}>
                <label>Villa Photo</label>
                <input
                  type="file"
                  accept="image/*"
                  onChange={async (e) => {
                    const file = e.target.files[0]
                    if (!file) return
                    const formData = new FormData()
                    formData.append("image", file)
                    const res = await fetch("https://villa-backend-1gzn.onrender.com/api/upload", {
                      method: "POST",
                      body: formData,
                    })
                    const data = await res.json()
                    if (data.success) {
                      setVillaForm({ ...villaForm, image: `https://villa-backend-1gzn.onrender.com${data.imageUrl}` })
                    }
                  }}
                />
                {villaForm.image && (
                  <img src={villaForm.image} alt="preview" style={{ marginTop: "8px", width: "100%", height: "120px", objectFit: "cover", borderRadius: "8px" }} />
                )}
              </div>

              <div className="search-field" style={{ marginBottom: "1rem" }}>
                <label>Rating</label>
                <select value={villaForm.rating} onChange={(e) => setVillaForm({ ...villaForm, rating: e.target.value })}>
                  <option>★★★★★</option>
                  <option>★★★★☆</option>
                  <option>★★★☆☆</option>
                </select>
              </div>

              <div className="search-field" style={{ marginBottom: "1rem" }}>
                <label>Card color</label>
                <input
                  type="color"
                  value={villaForm.color}
                  onChange={(e) => setVillaForm({ ...villaForm, color: e.target.value })}
                />
              </div>

              <div style={{ display: "flex", gap: "12px" }}>
                <button type="submit" className="search-btn">
                  {editingVilla ? "Update Villa" : "Add Villa"}
                </button>
                {editingVilla && (
                  <button
                    type="button"
                    onClick={() => { setEditingVilla(null); setVillaForm({ name: "", location: "", price: "", color: "#E1F5EE", image: "", desc: "", rating: "★★★★★" }) }}
                    style={{ background: "#ccc", color: "#333", border: "none", borderRadius: "8px", padding: "10px 20px", cursor: "pointer" }}
                  >
                    Cancel
                  </button>
                )}
              </div>
            </form>

            <h3 style={{ marginBottom: "1rem" }}>All Villas ({villas.length})</h3>
            {villas.length === 0 ? (
              <p style={{ color: "#888" }}>No villas yet. Add one above!</p>
            ) : (
              <div style={{ display: "flex", flexDirection: "column", gap: "12px" }}>
                {villas.map((villa) => (
                  <div key={villa._id} style={{ background: "white", border: "1px solid #eee", borderRadius: "12px", padding: "1rem", display: "flex", justifyContent: "space-between", alignItems: "center" }}>
                    <div>
                      <div style={{ fontWeight: "600" }}>{villa.name}</div>
                      <div style={{ fontSize: "13px", color: "#888" }}>📍 {villa.location} · ${villa.price}/night · {villa.rating}</div>
                    </div>
                    <div style={{ display: "flex", gap: "8px" }}>
                      <button onClick={() => handleEditVilla(villa)} className="search-btn" style={{ padding: "6px 14px", fontSize: "13px" }}>Edit</button>
                      <button onClick={() => handleDeleteVilla(villa._id)} style={{ background: "#fee2e2", color: "#dc2626", border: "none", borderRadius: "8px", padding: "6px 14px", fontSize: "13px", cursor: "pointer" }}>Delete</button>
                    </div>
                  </div>
                ))}
              </div>
            )}
          </div>
        )}

        {/* PACKAGES TAB */}
{tab === "packages" && (
  <div>
    <h3 style={{ marginBottom: "1rem" }}>Select a Villa</h3>
    <div className="search-field" style={{ marginBottom: "1.5rem", maxWidth: "300px" }}>
      <label>Villa</label>
      <select value={selectedVillaId} onChange={(e) => setSelectedVillaId(e.target.value)}>
        <option value="">-- Select a villa --</option>
        {villas.map((v) => (
          <option key={v._id} value={v._id}>{v.name}</option>
        ))}
      </select>
    </div>

    {selectedVillaId && (
      <div>
        <h3 style={{ marginBottom: "1rem" }}>{editingPackage ? "✏️ Edit Package" : "➕ Add Package"}</h3>
        <form onSubmit={handlePackageSubmit} style={{ maxWidth: "500px", marginBottom: "2rem" }}>
          <div className="search-field" style={{ marginBottom: "1rem" }}>
            <label>Package Name</label>
            <input type="text" placeholder="e.g. Deluxe Sea View Room" value={packageForm.name} onChange={(e) => setPackageForm({ ...packageForm, name: e.target.value })} required />
          </div>
          <div className="search-field" style={{ marginBottom: "1rem" }}>
            <label>Description</label>
            <input type="text" placeholder="e.g. 1 large double bed, sea view" value={packageForm.desc} onChange={(e) => setPackageForm({ ...packageForm, desc: e.target.value })} required />
          </div>
          <div className="search-field" style={{ marginBottom: "1rem" }}>
            <label>Number of Guests</label>
            <select value={packageForm.guests} onChange={(e) => setPackageForm({ ...packageForm, guests: e.target.value })}>
              <option value="1">1</option>
              <option value="2">2</option>
              <option value="4">4</option>
              <option value="6">6</option>
            </select>
          </div>
          <div className="search-field" style={{ marginBottom: "1rem" }}>
            <label>Price per night ($)</label>
            <input type="number" placeholder="e.g. 200" value={packageForm.price} onChange={(e) => setPackageForm({ ...packageForm, price: e.target.value })} required />
          </div>
          <div style={{ display: "flex", gap: "12px" }}>
            <button type="submit" className="search-btn">{editingPackage ? "Update Package" : "Add Package"}</button>
            {editingPackage && (
              <button type="button" onClick={() => { setEditingPackage(null); setPackageForm({ name: "", desc: "", guests: 2, price: "" }) }} style={{ background: "#ccc", color: "#333", border: "none", borderRadius: "8px", padding: "10px 20px", cursor: "pointer" }}>Cancel</button>
            )}
          </div>
        </form>

        <h3 style={{ marginBottom: "1rem" }}>Packages ({packages.length})</h3>
        {packages.length === 0 ? (
          <p style={{ color: "#888" }}>No packages yet. Add one above!</p>
        ) : (
          <div style={{ display: "flex", flexDirection: "column", gap: "12px" }}>
            {packages.map((pkg) => (
              <div key={pkg._id} style={{ background: "white", border: "1px solid #eee", borderRadius: "12px", padding: "1rem", display: "flex", justifyContent: "space-between", alignItems: "center" }}>
                <div>
                  <div style={{ fontWeight: "600" }}>{pkg.name}</div>
                  <div style={{ fontSize: "13px", color: "#888" }}>👥 {pkg.guests} guests · ${pkg.price}/night · {pkg.desc}</div>
                </div>
                <div style={{ display: "flex", gap: "8px" }}>
                  <button onClick={() => handleEditPackage(pkg)} className="search-btn" style={{ padding: "6px 14px", fontSize: "13px" }}>Edit</button>
                  <button onClick={() => handleDeletePackage(pkg._id)} style={{ background: "#fee2e2", color: "#dc2626", border: "none", borderRadius: "8px", padding: "6px 14px", fontSize: "13px", cursor: "pointer" }}>Delete</button>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>
    )}
  </div>
)}

        {/* BOOKINGS TAB */}
        {tab === "bookings" && (
          <div>
            <h3 style={{ marginBottom: "1rem" }}>All Bookings ({bookings.length})</h3>
            {bookings.length === 0 ? (
              <p style={{ color: "#888" }}>No bookings yet.</p>
            ) : (
              <div style={{ display: "flex", flexDirection: "column", gap: "12px" }}>
                {bookings.map((booking) => (
                  <div key={booking._id} style={{ background: "white", border: "1px solid #eee", borderRadius: "12px", padding: "1rem" }}>
                    <div style={{ fontWeight: "600", marginBottom: "4px" }}>{booking.villaName}</div>
                    <div style={{ fontSize: "13px", color: "#555" }}>
                      👤 {booking.name} · 📧 {booking.email}
                    </div>
                    <div style={{ fontSize: "13px", color: "#555" }}>
                      📅 {booking.checkIn} → {booking.checkOut} · {booking.nights} nights · ${booking.total}
                    </div>
                  </div>
                ))}
              </div>
            )}
          </div>
        )}
      </div>
    </div>
  )
}

export default Admin