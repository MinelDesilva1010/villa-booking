import { useState, useEffect } from "react"
import { useParams, Link } from "react-router-dom"

function VillaDetails() {
  const { id } = useParams()

  const [villa, setVilla] = useState(null)
  const [loading, setLoading] = useState(true)
  const [packages, setPackages] = useState([])

  const [reviews, setReviews] = useState([])
const [rating, setRating] = useState(5)
const [comment, setComment] = useState("")
const [reviewSubmitted, setReviewSubmitted] = useState(false)
const loggedInUser = localStorage.getItem("userName")

  // Fetch villa from backend
 useEffect(() => {
  fetch("http://localhost:5000/api/villas")
    .then((res) => res.json())
    .then((data) => {
      const found = data.find((v) => v._id === id)
      setVilla(found)
      setLoading(false)
      // Fetch packages for this villa
      if (found) {
        fetch(`http://localhost:5000/api/packages/${found._id}`)
          .then((res) => res.json())
          .then((pkgs) => setPackages(pkgs))
      }

      fetch(`http://localhost:5000/api/reviews/${id}`)
  .then((res) => res.json())
  .then((data) => setReviews(data))


    })
}, [id])

  // --- Booking form state ---
  const [showForm, setShowForm] = useState(false)
  const [checkIn, setCheckIn] = useState("")
  const [checkOut, setCheckOut] = useState("")
  const [guests, setGuests] = useState(2)
  const [name, setName] = useState("")
  const [email, setEmail] = useState("")
  const [confirmed, setConfirmed] = useState(false)

  if (loading) return <div className="section"><p>Loading villa...</p></div>

  if (!villa) {
    return <div className="section"><p>Villa not found.</p><Link to="/">Back home</Link></div>
  }

  // Calculate number of nights and total price
  let nights = 0
  let total = 0
  if (checkIn && checkOut) {
    const inDate = new Date(checkIn)
    const outDate = new Date(checkOut)
    nights = Math.round((outDate - inDate) / (1000 * 60 * 60 * 24))
    if (nights > 0) total = nights * villa.price
  }

  async function handleReviewSubmit(e) {
  e.preventDefault()
  const res = await fetch("http://localhost:5000/api/reviews", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({
      villaId: id,
      userName: loggedInUser,
      rating: Number(rating),
      comment,
    }),
  })
  const data = await res.json()
  if (data.success) {
    setReviewSubmitted(true)
    setComment("")
    setRating(5)
    // Refresh reviews
    fetch(`http://localhost:5000/api/reviews/${id}`)
      .then((res) => res.json())
      .then((data) => setReviews(data))
  }
}

  async function handleSubmit(e) {
    e.preventDefault()

    const bookingData = {
      villaId: villa.id,
      villaName: villa.name,
      checkIn,
      checkOut,
      guests,
      name,
      email,
      total,
      nights,
    }

    try {
      const response = await fetch("http://localhost:5000/api/bookings", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(bookingData),
      })

      const data = await response.json()

      if (data.success) {
        setConfirmed(true)
      }
    } catch (error) {
      console.error("Booking failed:", error)
      alert("Something went wrong. Please try again.")
    }
  }

  return (
    <div>
      <nav className="navbar">
        <Link to="/" style={{ textDecoration: "none" }}><div className="logo">Villa<span>Stay</span></div></Link>
        <div className="nav-links">
          <Link to="/">Home</Link>
        </div>
      </nav>

      <div className="section">
        <div className="villa-img" style={{ background: villa.color, height: "220px", borderRadius: "12px", overflow: "hidden", fontSize: "80px" }}>
          {villa.image
            ? <img src={villa.image} alt={villa.name} style={{ width: "100%", height: "100%", objectFit: "cover" }} />
            : villa.emoji
          }
        </div>

        <h1 style={{ marginTop: "1.5rem" }}>{villa.name}</h1>
        <p style={{ color: "#888", marginBottom: "1rem" }}>📍 {villa.location} &nbsp;·&nbsp; {villa.rating}</p>
        <p style={{ marginBottom: "1.5rem", lineHeight: "1.6" }}>{villa.desc}</p>

        <div className="villa-price" style={{ fontSize: "20px", marginBottom: "1.5rem" }}>
          ${villa.price} <span style={{ fontSize: "14px" }}>/ night</span>
        </div>

        {packages.length > 0 && (
  <div style={{ marginBottom: "2rem" }}>
    <h3 style={{ marginBottom: "1rem", fontSize: "18px" }}>🏨 Room Packages</h3>
    <div style={{ display: "flex", flexDirection: "column", gap: "12px" }}>
      {packages.map((pkg) => (
        <div key={pkg._id} style={{ background: "white", border: "1px solid #eee", borderRadius: "12px", padding: "1rem", display: "flex", justifyContent: "space-between", alignItems: "center" }}>
          <div>
            <div style={{ fontWeight: "600", marginBottom: "4px" }}>{pkg.name}</div>
            <div style={{ fontSize: "13px", color: "#888", marginBottom: "4px" }}>{pkg.desc}</div>
            <div style={{ fontSize: "13px", color: "#888" }}>👥 {pkg.guests} guests</div>
          </div>
          <div style={{ textAlign: "right" }}>
            <div style={{ fontWeight: "600", color: "#0F6E56", fontSize: "16px", marginBottom: "8px" }}>${pkg.price}<span style={{ fontSize: "12px", fontWeight: "400", color: "#888" }}>/night</span></div>
            <button
              className="search-btn"
              style={{ padding: "6px 16px", fontSize: "13px" }}
              onClick={() => setShowForm(true)}
            >
              Book
            </button>
          </div>
        </div>
      ))}
    </div>
  </div>
)}

        {!showForm && !confirmed && (
          <button className="search-btn" onClick={() => setShowForm(true)}>
            Book this villa
          </button>
        )}

        {showForm && !confirmed && (
          <form onSubmit={handleSubmit} style={{ maxWidth: "400px", marginTop: "1rem" }}>
            <div className="search-field" style={{ marginBottom: "1rem" }}>
              <label>Check in</label>
              <input type="date" value={checkIn} onChange={(e) => setCheckIn(e.target.value)} required />
            </div>
            <div className="search-field" style={{ marginBottom: "1rem" }}>
              <label>Check out</label>
              <input type="date" value={checkOut} onChange={(e) => setCheckOut(e.target.value)} required />
            </div>
            <div className="search-field" style={{ marginBottom: "1rem" }}>
              <label>Guests</label>
              <select value={guests} onChange={(e) => setGuests(e.target.value)}>
                <option value="2">2</option>
                <option value="4">4</option>
                <option value="6">6</option>
                <option value="8">8+</option>
              </select>
            </div>
            <div className="search-field" style={{ marginBottom: "1rem" }}>
              <label>Full name</label>
              <input type="text" value={name} onChange={(e) => setName(e.target.value)} placeholder="Your name" required />
            </div>
            <div className="search-field" style={{ marginBottom: "1rem" }}>
              <label>Email</label>
              <input type="email" value={email} onChange={(e) => setEmail(e.target.value)} placeholder="you@example.com" required />
            </div>

            {nights > 0 && (
              <p style={{ marginBottom: "1rem", fontWeight: "600" }}>
                {nights} night{nights > 1 ? "s" : ""} × ${villa.price} = ${total}
              </p>
            )}

            <button type="submit" className="search-btn" style={{ width: "100%" }}>
              Confirm booking
            </button>
          </form>
        )}

        {confirmed && (
          <div style={{ background: "#E1F5EE", padding: "1.5rem", borderRadius: "12px", maxWidth: "400px" }}>
            <h3 style={{ marginBottom: "0.5rem" }}>🎉 Booking confirmed!</h3>
            <p>Thanks {name}, your stay at {villa.name} from {checkIn} to {checkOut} ({nights} nights) is booked.</p>
            <p style={{ marginTop: "0.5rem", fontWeight: "600" }}>Total: ${total}</p>
          </div>
        )}

        {/* REVIEWS SECTION */}
<div style={{ marginTop: "2rem" }}>
  <h3 style={{ fontSize: "18px", marginBottom: "1rem" }}>⭐ Guest Reviews ({reviews.length})</h3>

  {/* Review cards */}
  {reviews.length === 0 ? (
    <p style={{ color: "#888", marginBottom: "1.5rem" }}>No reviews yet — be the first!</p>
  ) : (
    <div style={{ display: "flex", flexDirection: "column", gap: "12px", marginBottom: "1.5rem" }}>
      {reviews.map((review) => (
        <div key={review._id} style={{ background: "white", border: "1px solid #eee", borderRadius: "12px", padding: "1rem" }}>
          <div style={{ display: "flex", justifyContent: "space-between", marginBottom: "6px" }}>
            <div style={{ fontWeight: "600" }}>
              <span style={{ background: "#1D9E75", color: "white", borderRadius: "50%", padding: "4px 10px", marginRight: "8px", fontSize: "13px" }}>
                {review.userName?.charAt(0).toUpperCase()}
              </span>
              {review.userName}
            </div>
            <div style={{ color: "#BA7517" }}>
              {"★".repeat(review.rating)}{"☆".repeat(5 - review.rating)}
            </div>
          </div>
          <p style={{ fontSize: "14px", color: "#555", lineHeight: "1.6" }}>{review.comment}</p>
        </div>
      ))}
    </div>
  )}

  {/* Review form */}
  {loggedInUser ? (
    reviewSubmitted ? (
      <div style={{ background: "#E1F5EE", padding: "1rem", borderRadius: "12px" }}>
        <p>✅ Thanks for your review, {loggedInUser}!</p>
      </div>
    ) : (
      <form onSubmit={handleReviewSubmit} style={{ maxWidth: "500px" }}>
        <h4 style={{ marginBottom: "1rem" }}>Leave a review</h4>
        <div className="search-field" style={{ marginBottom: "1rem" }}>
          <label>Rating</label>
          <select value={rating} onChange={(e) => setRating(e.target.value)}>
            <option value="5">★★★★★ (5)</option>
            <option value="4">★★★★☆ (4)</option>
            <option value="3">★★★☆☆ (3)</option>
            <option value="2">★★☆☆☆ (2)</option>
            <option value="1">★☆☆☆☆ (1)</option>
          </select>
        </div>
        <div className="search-field" style={{ marginBottom: "1rem" }}>
          <label>Your review</label>
          <textarea
            value={comment}
            onChange={(e) => setComment(e.target.value)}
            placeholder="Share your experience..."
            required
            style={{ width: "100%", border: "1px solid #ddd", borderRadius: "8px", padding: "8px 10px", fontSize: "14px", minHeight: "100px", resize: "vertical" }}
          />
        </div>
        <button type="submit" className="search-btn">Submit Review</button>
      </form>
    )
  ) : (
    <div style={{ background: "#f9f9f9", padding: "1rem", borderRadius: "12px", border: "1px solid #eee" }}>
      <p style={{ fontSize: "14px", color: "#555" }}>
        <Link to="/login" style={{ color: "#1D9E75", fontWeight: "600" }}>Sign in</Link> to leave a review.
      </p>
    </div>
  )}
</div>


      </div>
    </div>
  )
}

export default VillaDetails