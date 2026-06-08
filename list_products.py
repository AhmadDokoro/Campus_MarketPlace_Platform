"""
UMT Campus Marketplace — Bulk product lister
Logs in as two sellers and creates 50 products each (100 total).
Uses Pexels API for images; falls back to a placeholder if no result.
"""

import requests
import time
import json
import re
from bs4 import BeautifulSoup

BASE_URL = "https://umtmarket.ahsmart.dev"
PEXELS_KEY = "KndinDCCv9RLjTWL1kJ0n7EYaKPIGdTqmWnWIDzHjanOsOCrwH8meGaz"

SELLERS = [
    {"email": "Seller@umt.edu.my",    "password": "Seller123"},
    {"email": "abubakar@gmail.com",   "password": "abubakar123"},
]

# 100 campus-relevant products for UMT (University Malaysia Terengganu)
# Each entry: (title, description, price_MYR, quantity, condition, pexels_query)
ALL_PRODUCTS = [
    # ── Seller 1 — 50 products ──────────────────────────────────────────────
    # Electronics & Tech
    ("Casio FX-570ES Plus Scientific Calculator",
     "Essential scientific calculator for engineering and science students. Supports 417 functions. Perfect condition.",
     55.00, 3, "NEW", "scientific calculator"),

    ("USB-C 7-in-1 Hub Multiport Adapter",
     "Connect HDMI, USB 3.0, SD card, and more. Compatible with MacBook and all USB-C laptops.",
     89.00, 2, "NEW", "usb hub laptop"),

    ("Wireless Bluetooth Earbuds TWS",
     "Crystal clear audio with active noise cancellation. 30-hour battery life with charging case.",
     79.00, 4, "NEW", "wireless earbuds"),

    ("Adjustable Aluminium Laptop Stand",
     "Ergonomic stand reduces neck strain during long study sessions. Foldable and portable.",
     45.00, 5, "NEW", "laptop stand desk"),

    ("Portable Power Bank 20000mAh",
     "Fast-charge compatible power bank. Charges your phone 5-6 times. LED battery indicator.",
     75.00, 3, "NEW", "power bank portable charger"),

    ("SanDisk 64GB USB 3.0 Flash Drive",
     "Transfer files at up to 100MB/s. Compact design fits any USB port. For assignments and project files.",
     25.00, 8, "NEW", "usb flash drive"),

    ("Laptop Cooling Pad with 4 Fans",
     "Keeps your laptop cool during intense gaming or coding sessions. Adjustable fan speed.",
     55.00, 3, "NEW", "laptop cooling pad"),

    ("Compact Bluetooth Speaker",
     "IPX5 waterproof. 12-hour playtime. Great for hostel room gatherings. Loud and clear bass.",
     65.00, 4, "NEW", "bluetooth speaker compact"),

    ("HD 1080p USB Webcam",
     "Perfect for online lectures and Zoom meetings. Plug-and-play, no driver needed.",
     69.00, 2, "NEW", "webcam computer"),

    ("RGB Mechanical Keyboard Compact TKL",
     "Tactile brown switches. Backlit with 18 RGB modes. Great for coding and long typing sessions.",
     145.00, 2, "NEW", "mechanical keyboard"),

    # Books & Stationery
    ("Engineering Mathematics Textbook (8th Ed)",
     "Comprehensive engineering maths reference. Covers calculus, differential equations and more.",
     85.00, 1, "USED", "mathematics textbook engineering"),

    ("Highlighter Pastel Set (8 Colours)",
     "Chisel tip for broad and fine lines. Smear-proof on most printer inks. Great for exam revision.",
     12.00, 15, "NEW", "highlighter pens colorful"),

    ("Staedtler Mechanical Pencil Set (0.5mm)",
     "Precision mechanical pencils for technical drawing and notes. 3 pencils + refills included.",
     18.00, 10, "NEW", "mechanical pencil stationery"),

    ("A4 Ring Binder Folders Set of 3",
     "Heavy-duty PVC binders with clear front pocket. Holds up to 200 sheets each.",
     22.00, 6, "NEW", "binder folder office"),

    ("Graph Paper Spiral Notebook A4",
     "100 pages 5mm grid paper. Ideal for math, physics, and engineering drawings.",
     9.50, 20, "NEW", "graph paper notebook"),

    ("Sticky Notes Value Pack (600 sheets)",
     "6 pastel colours, 3 sizes. Strong adhesive stays put. Perfect for revision and reminders.",
     15.00, 12, "NEW", "sticky notes colorful"),

    ("Pilot Frixion Erasable Pen Set (12 colours)",
     "Write, erase and rewrite. Heat-erasable gel ink. Popular among UMT students.",
     35.00, 8, "NEW", "colored pens writing"),

    ("Document File Box Organiser",
     "Stackable A4 document trays. Keeps assignments and lecture notes neatly sorted.",
     28.00, 5, "NEW", "file organizer desk"),

    ("Scientific Lab Notebook Hardcover",
     "Numbered pages with signature lines. Sewn binding. Perfect for chemistry and biology lab reports.",
     19.00, 10, "NEW", "lab notebook science"),

    ("Whiteboard 60x45cm with Markers",
     "Small personal whiteboard for hostel room study. Includes 3 markers and eraser.",
     38.00, 4, "NEW", "whiteboard markers"),

    # Food & Beverages — Malay campus foods
    ("Homemade Nasi Lemak (per pack)",
     "Authentic fragrant coconut rice with sambal, anchovies, egg and cucumber. Freshly prepared daily.",
     5.50, 20, "NEW", "nasi lemak malaysian food"),

    ("Keropok Lekor Terengganu (500g)",
     "Fresh fish crackers from Terengganu. A local specialty! Crispy and delicious.",
     12.00, 15, "NEW", "keropok lekor fish cracker"),

    ("Kuih Lapis Assorted Box (12 pieces)",
     "Colourful layered kuih made fresh. Mixed flavours: pandan, coconut, ubi. Perfect for tea time.",
     15.00, 10, "NEW", "kuih lapis traditional malay"),

    ("Homemade Rendang Daging (250g)",
     "Rich slow-cooked beef rendang with authentic Kelantanese spices. Frozen, ready to reheat.",
     22.00, 8, "NEW", "rendang beef malay food"),

    ("Dodol Terengganu Original (500g)",
     "Traditional sticky sweet made with glutinous rice and coconut milk. A Terengganu pride.",
     18.00, 12, "NEW", "dodol malay sweet"),

    ("Teh Tarik Premix Sachets (20pcs)",
     "Creamy pulled tea taste at home. Each sachet makes 1 cup. Halal certified.",
     12.00, 25, "NEW", "teh tarik tea malaysia"),

    ("Sambal Belacan Homemade (200g)",
     "Spicy shrimp paste sambal made fresh. No preservatives. Great with rice or as dipping sauce.",
     8.00, 20, "NEW", "sambal chili paste"),

    ("Kuih Talam Pandan (10 pieces)",
     "Soft two-layered kuih with pandan bottom and coconut top. Made fresh every morning.",
     10.00, 15, "NEW", "pandan kuih traditional"),

    ("Instant Laksa Paste (250g)",
     "Homemade Terengganu-style laksa paste. Just add noodles and coconut milk. Feeds 4 people.",
     14.00, 10, "NEW", "laksa noodles soup"),

    ("Onde-onde Homemade (15 pieces)",
     "Glutinous rice balls filled with melted gula melaka, rolled in fresh coconut. Order by 8pm.",
     12.00, 10, "NEW", "onde onde malay kuih"),

    # Room & Hostel Items
    ("Mini USB Desk Fan 5V",
     "Silent and portable. Powered by USB. Ideal for hostel desks during hot afternoons.",
     22.00, 8, "NEW", "mini desk fan"),

    ("LED Bedside Desk Lamp with USB Port",
     "3 colour modes, 10 brightness levels. Eye-care technology for late-night studying.",
     45.00, 5, "NEW", "desk lamp study"),

    ("Storage Box Set (4 pcs, collapsible)",
     "Foldable fabric storage cubes. Organise books, clothes and accessories in your hostel room.",
     35.00, 6, "NEW", "storage box organizer"),

    ("Portable Clothes Drying Rack",
     "Foldable rack with 16 rails. Space-saving design for hostel rooms. Holds up to 15kg.",
     55.00, 4, "NEW", "clothes drying rack"),

    ("Extension Cord 3-socket with USB (3m)",
     "Surge-protected extension with 3 power sockets and 2 USB ports. Essential for any hostel room.",
     35.00, 7, "NEW", "extension cord power strip"),

    ("Premium Pillow with Memory Foam Insert",
     "Contour memory foam pillow for deep sleep. Standard pillowcase size. Washable cover.",
     55.00, 5, "NEW", "pillow memory foam sleep"),

    ("Wall-Mounted Shoe Rack (4-tier)",
     "Saves floor space. Holds 8 pairs. Easy assembly, no tools required.",
     42.00, 4, "NEW", "shoe rack wall mount"),

    ("Microfibre Bath Towel Set (2 pcs)",
     "Super absorbent and quick-drying. Includes 1 bath towel + 1 face towel. Machine washable.",
     28.00, 8, "NEW", "bath towel set"),

    ("Multipurpose Hook Set (20 pcs)",
     "Self-adhesive stainless steel hooks. No drilling. Holds keys, bags, cables and more.",
     15.00, 15, "NEW", "adhesive hooks wall"),

    ("Table Calendar Planner 2026",
     "Academic desk calendar with weekly and monthly view. Includes space for notes and deadlines.",
     12.00, 20, "NEW", "desk calendar planner"),

    # Sports & Fitness
    ("Yonex Voltric 1 Badminton Racket",
     "Lightweight graphite frame. Great for intermediate players. Includes cover.",
     89.00, 2, "USED", "badminton racket"),

    ("Yoga Mat Anti-slip 6mm",
     "Thick non-slip mat for yoga, pilates and stretching. Includes carrying strap.",
     45.00, 5, "NEW", "yoga mat exercise"),

    ("Resistance Bands Set (5 levels)",
     "Latex exercise bands for home workouts. From light to extra heavy resistance.",
     28.00, 8, "NEW", "resistance bands workout"),

    ("Nike Sport Water Bottle 1L",
     "BPA-free. Wide mouth for easy filling and cleaning. Leak-proof lid.",
     35.00, 6, "NEW", "sport water bottle"),

    ("Jump Rope Speed Skipping Rope",
     "Adjustable steel cable with comfortable foam handles. For cardio and fitness training.",
     18.00, 10, "NEW", "jump rope skipping"),

    ("Adidas Sports Backpack",
     "Spacious main compartment with laptop sleeve and separate shoes compartment.",
     95.00, 2, "USED", "sports backpack"),

    ("Gym Gloves Wrist Support",
     "Anti-slip palm padding. Adjustable wrist strap. For weight training and pull-ups.",
     25.00, 6, "NEW", "gym gloves fitness"),

    ("Football Nike Size 5",
     "Durable machine-stitched football. Great for casual games on campus field.",
     55.00, 3, "USED", "football soccer ball"),

    ("Knee Support Brace Pair",
     "Compression sleeve for running, cycling and sports. Prevents injury.",
     32.00, 6, "NEW", "knee support brace"),

    ("Running Armband Phone Holder",
     "Fits phones up to 6.7 inch. Sweat-proof. Reflective strip for night running.",
     18.00, 10, "NEW", "phone armband running"),

    # ── Seller 2 — 50 products ──────────────────────────────────────────────
    # Clothing & Fashion
    ("Baju Kurung Moden Cotton (Women)",
     "Elegant modern baju kurung in soft cotton. Available in dusty rose and mint green. S-XL.",
     85.00, 3, "NEW", "baju kurung malaysia fashion"),

    ("Batik Shirt Men Short Sleeve",
     "Beautiful hand-printed batik shirt. Traditional yet modern design. Perfect for formal events.",
     65.00, 4, "NEW", "batik shirt men malaysia"),

    ("UMT Varsity Hoodie Navy Blue",
     "Official UMT-style hoodie with embroidered logo. Thick cotton blend for cool nights.",
     75.00, 5, "NEW", "university hoodie college"),

    ("Tudung Bawal Plain (5 colours)",
     "High-quality satin bawal shawl. Non-slip, easy to style. Available in 5 colours.",
     15.00, 20, "NEW", "hijab tudung muslim fashion"),

    ("Formal Shirt White Long Sleeve (Men)",
     "Crisp white formal shirt for presentations and events. Slim-fit cut. Wrinkle-resistant.",
     45.00, 5, "NEW", "white formal shirt men"),

    ("Songket Mini Clutch Bag",
     "Handmade Terengganu songket fabric clutch. Gold and red traditional motif. Unique gift.",
     55.00, 3, "NEW", "songket bag malaysia"),

    ("Tie-Dye T-Shirt Unisex",
     "Hand-dyed unique patterns. No two are the same. 100% cotton. Trendy campus wear.",
     28.00, 10, "NEW", "tie dye tshirt colorful"),

    ("Women Blazer Slim Fit",
     "Professional blazer for FYP presentations and interviews. Polyester blend, easy iron.",
     88.00, 2, "NEW", "blazer women formal"),

    ("Canvas Tote Bag UMT Design",
     "Eco-friendly heavy-duty canvas tote. Printed UMT campus design. Fits A4 books.",
     22.00, 15, "NEW", "canvas tote bag"),

    ("Slip-on Canvas Sneakers",
     "Comfortable everyday slip-on. Lightweight and breathable. US6–10 available.",
     55.00, 4, "NEW", "canvas sneakers shoes"),

    # Beauty & Personal Care
    ("Sunscreen SPF50+ Matte Finish 50ml",
     "Lightweight, non-greasy sunscreen ideal for Malaysia's hot weather. PA+++ rated. Halal.",
     32.00, 10, "NEW", "sunscreen spf50 skincare"),

    ("Skincare Starter Kit (Halal Certified)",
     "Complete 4-step routine: cleanser, toner, moisturiser, SPF. Suitable for all skin types.",
     75.00, 5, "NEW", "skincare set beauty"),

    ("Rose Water Facial Mist 100ml",
     "Hydrating facial spray with natural rose extract. Refreshes skin between classes.",
     18.00, 12, "NEW", "rose water facial mist"),

    ("Eyebrow Pencil Waterproof",
     "Precise micro-fork tip. Long lasting 24h. Available in dark brown, black and ash grey.",
     12.00, 20, "NEW", "eyebrow pencil makeup"),

    ("Professional Makeup Brush Set (12pcs)",
     "Cruelty-free synthetic brushes. Includes blush, contour, eyeshadow and blending brushes.",
     45.00, 5, "NEW", "makeup brush set professional"),

    ("Attar Oud Perfume Oil 12ml",
     "Concentrated long-lasting oud perfume. Alcohol-free, halal certified. Elegant fragrance.",
     38.00, 8, "NEW", "oud perfume attar"),

    ("Lip Tint with SPF15 (3 shades)",
     "Sheer moisturising lip tint. Buildable colour. Everyday natural look. Rose, berry, coral.",
     15.00, 15, "NEW", "lip tint makeup beauty"),

    ("Konjac Facial Sponge Set (2pcs)",
     "Natural plant-based facial sponge. Gentle exfoliation for sensitive skin.",
     12.00, 18, "NEW", "facial sponge skincare"),

    ("Hair Serum Argan Oil 100ml",
     "Frizz-control argan oil serum. Heat protectant up to 230°C. Suitable for all hair types.",
     28.00, 10, "NEW", "hair serum argan oil"),

    ("Nail Art Starter Kit",
     "Includes base coat, 6 colours, top coat and nail art tools. Everything for DIY manicure.",
     35.00, 8, "NEW", "nail art kit manicure"),

    # More Malay Foods
    ("Nasi Kerabu Biru (per pack)",
     "Authentic Kelantanese blue rice with budu sauce, coconut, fish, and ulam. Order by 7pm.",
     7.00, 15, "NEW", "nasi kerabu blue rice"),

    ("Ayam Percik Terengganu (2 pieces)",
     "Grilled chicken coated in rich coconut milk sauce. A Terengganu favourite. Ready to eat.",
     12.00, 10, "NEW", "ayam percik grilled chicken"),

    ("Kuih Akok Terengganu (10 pieces)",
     "Traditional Terengganu egg kuih baked in a clay mould. Sweet and crispy edges.",
     10.00, 12, "NEW", "kuih malay traditional baked"),

    ("Homemade Pineapple Jam Tart (20pcs)",
     "Buttery pastry with homemade pineapple filling. Perfect for snacking between classes.",
     18.00, 10, "NEW", "pineapple tart cookies"),

    ("Roti John Beef (2 pcs)",
     "Crispy baguette stuffed with minced beef, egg and onion. Malaysian street food classic.",
     8.00, 15, "NEW", "roti john beef sandwich"),

    ("Pulut Kuning (per box)",
     "Yellow glutinous rice cooked in coconut milk and turmeric. Served with beef rendang.",
     14.00, 10, "NEW", "pulut kuning malay rice"),

    ("Cendol with Gula Melaka (per cup)",
     "Fresh pandan jelly, red bean, coconut milk and rich palm sugar syrup. Cold and refreshing.",
     4.50, 25, "NEW", "cendol dessert malaysia"),

    ("Homemade Cookies Assorted (400g)",
     "Freshly baked mixed cookies: chocolate chip, butter, oat and almond. Great study snack.",
     20.00, 12, "NEW", "cookies assorted homemade baked"),

    ("Mee Goreng Mamak Paste (200g)",
     "Ready-made mamak fried noodle paste. Just add noodles, eggs and vegetables. Serves 3-4.",
     10.00, 20, "NEW", "mee goreng noodles fry"),

    ("Air Sirap Bandung Premix (500g)",
     "Classic pink rose syrup drink mix. Just add water and ice. Nostalgic Malaysian taste.",
     8.00, 20, "NEW", "bandung rose syrup drink"),

    # Services & Handcraft
    ("Thesis Binding Service (per copy)",
     "Professional thesis hard cover binding with gold lettering. Ready in 24 hours. A4 size.",
     25.00, 50, "NEW", "thesis book binding"),

    ("Assignment Printing Service (per page)",
     "Laser printing, double-sided option. Collect at Block K hostel. Black & white or colour.",
     0.30, 500, "NEW", "printing service paper"),

    ("Laptop Repair & Cleaning Service",
     "Virus removal, thermal paste replacement, screen repair. Free diagnosis. Contact for quote.",
     50.00, 10, "NEW", "laptop repair service technician"),

    ("Private Tutor — Calculus & Statistics",
     "Final year engineering student offering math tutoring. RM25/hour. Online or in person.",
     25.00, 20, "NEW", "tutor studying math"),

    ("Private Tutor — Organic Chemistry",
     "Second-year chemistry student. Covers all UMT chemistry modules. RM20/hour.",
     20.00, 20, "NEW", "chemistry science study"),

    ("Handmade Beaded Bracelet",
     "Custom name or message bracelets. Handcrafted with Japanese seed beads. 2-day turnaround.",
     15.00, 30, "NEW", "beaded bracelet handmade jewelry"),

    ("Traditional Terengganu Songket Bookmark",
     "Miniature songket weave bookmark. Unique Terengganu craft souvenir. Set of 3.",
     18.00, 20, "NEW", "songket fabric traditional craft"),

    ("Custom Printed Phone Case",
     "Upload your design or choose from templates. Hard plastic case. All iPhone and Samsung models.",
     22.00, 25, "NEW", "custom phone case"),

    ("Scented Soy Wax Candle Handmade",
     "Hand-poured soy wax candle in jasmine, vanilla and oud scents. 30-hour burn time.",
     28.00, 15, "NEW", "soy candle scented handmade"),

    ("Mini Succulent Plant (pot included)",
     "Low-maintenance succulent in a cute ceramic pot. Perfect for brightening your hostel desk.",
     12.00, 20, "NEW", "succulent plant small pot"),

    ("Bicycle D-Lock Heavy Duty",
     "High-security hardened steel D-lock. Ideal for campus bike parking.",
     38.00, 5, "NEW", "bicycle lock security"),

    ("Motorcycle Chain Lock 1.2m",
     "Heavy-duty chrome-molybdenum chain lock. Includes 2 keys. Prevents theft at hostel parking.",
     55.00, 4, "NEW", "motorcycle chain lock security"),

    ("Portable Mini Fridge 6L (hostel safe)",
     "Compact thermoelectric mini fridge. Quiet operation, no compressor. Keeps drinks cold.",
     145.00, 2, "NEW", "mini fridge portable compact"),

    ("Digital Kitchen Scale 5kg",
     "Accurate to 1g. Tare function. Ideal for measuring ingredients for homemade food business.",
     28.00, 6, "NEW", "kitchen scale digital"),

    ("Portable Sewing Kit Travel Size",
     "21-piece sewing kit in a compact case. Thread, needles, scissors, buttons. Hostel essential.",
     12.00, 15, "NEW", "sewing kit portable"),

    ("Reusable Coffee Cup Travel Mug 350ml",
     "BPA-free. Double-wall insulated. Keeps coffee hot for 4 hours. Perfect for morning lectures.",
     35.00, 8, "NEW", "travel mug coffee cup"),

    ("Hand Sanitizer Spray 100ml (6-pack)",
     "70% alcohol, kills 99.9% of germs. Portable size for bags. Halal certified.",
     28.00, 10, "NEW", "hand sanitizer antibacterial"),

    ("USB LED Strip Lights 3m (RGB)",
     "Stick-on RGB LED strip with remote. Creates ambient lighting in hostel room. USB powered.",
     25.00, 10, "NEW", "led strip lights rgb room"),

    ("Instant Ramen Noodles Bundle (10 packs)",
     "Mix of Maggi, Mamee and MyKuali flavours. Late-night study essential. Halal certified.",
     18.00, 20, "NEW", "instant ramen noodles food"),

    ("Foldable Study Table Laptop Bed Tray",
     "Adjustable height and angle. Lightweight bamboo surface. Study in bed or on the floor.",
     65.00, 5, "NEW", "bed table laptop tray foldable"),
]

# ── Helpers ──────────────────────────────────────────────────────────────────

def get_pexels_image_url(query: str) -> str | None:
    """Search Pexels and return the medium-size URL of the first landscape result."""
    try:
        resp = requests.get(
            "https://api.pexels.com/v1/search",
            headers={"Authorization": PEXELS_KEY},
            params={"query": query, "per_page": 5, "orientation": "landscape"},
            timeout=10,
        )
        data = resp.json()
        photos = data.get("photos", [])
        if photos:
            return photos[0]["src"]["medium"]
    except Exception as e:
        print(f"  [Pexels] error for '{query}': {e}")
    return None


def login(session: requests.Session, email: str, password: str) -> bool:
    """POST to /signin and return True if login succeeds."""
    resp = session.post(
        f"{BASE_URL}/signin",
        data={"email": email, "password": password},
        allow_redirects=True,
        timeout=15,
    )
    # If we land on a page that isn't signin, assume success
    if "/signin" not in resp.url and resp.status_code < 400:
        print(f"  Logged in as {email} -> {resp.url}")
        return True
    print(f"  Login FAILED for {email} (landed on {resp.url})")
    return False


def get_categories(session: requests.Session) -> dict[str, int]:
    """Fetch /seller/products/new and parse category options."""
    resp = session.get(f"{BASE_URL}/seller/products/new", timeout=15)
    soup = BeautifulSoup(resp.text, "html.parser")
    cats = {}
    for opt in soup.select("select[name='categoryId'] option"):
        val = opt.get("value", "").strip()
        text = opt.get_text(strip=True)
        if val and val.isdigit():
            cats[text] = int(val)
    return cats


def create_product(session: requests.Session, cat_id: int, title: str,
                   description: str, price: float, quantity: int,
                   condition: str, image_url: str | None) -> bool:
    """POST to /seller/products and return True on success."""
    data = {
        "categoryId": str(cat_id),
        "title": title,
        "description": description,
        "price": f"{price:.2f}",
        "quantity": str(quantity),
        "condition": condition,
    }
    if image_url:
        data["imageUrl"] = image_url

    resp = session.post(
        f"{BASE_URL}/seller/products",
        data=data,
        allow_redirects=True,
        timeout=20,
    )
    success = "success" in resp.url or resp.status_code < 400
    return success


def pick_category(cats: dict, title: str) -> int:
    """
    Best-effort category picker based on product title keywords.
    Returns the first matching category ID, or the first category if nothing matches.
    """
    t = title.lower()
    mapping = [
        (["calculator","laptop","keyboard","mouse","webcam","speaker","hub","earbuds",
          "power bank","flash drive","cooling","fan","strip light","led"],
         ["electronics","tech","gadget","computer","digital","electrical"]),
        (["textbook","notebook","binder","pencil","pen","highlighter","sticky","folder",
          "whiteboard","calendar","lab notebook","stationery"],
         ["book","stationery","school","study","academic","writing"]),
        (["nasi","kuih","rendang","keropok","dodol","teh","sambal","laksa","onde",
          "ayam","pulut","cendol","roti","mee","sirap","cookies","jam tart","akok"],
         ["food","beverage","drink","meal","snack","kitchen"]),
        (["baju","batik","hoodie","shirt","tudung","blazer","tote","sneaker","songket clutch",
          "tie-dye","blouse","cloth","fashion"],
         ["fashion","cloth","wear","apparel","style","garment"]),
        (["sunscreen","skincare","makeup","perfume","lip","hair serum","nail","facial","rose water",
          "attar","konjac"],
         ["beauty","personal care","cosmetic","health","skincare"]),
        (["badminton","yoga","resistance","water bottle","jump rope","backpack",
          "gym gloves","football","knee support","armband"],
         ["sport","fitness","outdoor","gym","exercise","recreation"]),
        (["pillow","towel","storage","drying rack","extension","lamp","shoe rack",
          "hook set","mini fridge","bed tray","sewing","fan"],
         ["room","hostel","home","dorm","household","living"]),
        (["tutor","printing","binding","repair","photography","design","service"],
         ["service","handcraft","craft","custom","skill"]),
        (["bracelet","bookmark","candle","phone case","succulent","craft"],
         ["handcraft","craft","art","handmade","accessory"]),
        (["bicycle","motorcycle","d-lock","chain lock"],
         ["vehicle","transport","bike","motor","outdoor"]),
    ]
    for keywords, cat_keywords in mapping:
        if any(k in t for k in keywords):
            for cat_name, cat_id in cats.items():
                cn = cat_name.lower()
                if any(ck in cn for ck in cat_keywords):
                    return cat_id
    # fallback: first category
    return list(cats.values())[0]


# ── Main ─────────────────────────────────────────────────────────────────────

def run():
    products_s1 = ALL_PRODUCTS[:50]
    products_s2 = ALL_PRODUCTS[50:]

    results = {"success": 0, "failed": 0}

    for seller_idx, (seller, products) in enumerate(zip(SELLERS, [products_s1, products_s2])):
        print(f"\n{'='*60}")
        print(f"SELLER {seller_idx+1}: {seller['email']}")
        print(f"{'='*60}")

        session = requests.Session()
        session.headers.update({"User-Agent": "Mozilla/5.0 CampusMktBot/1.0"})

        if not login(session, seller["email"], seller["password"]):
            print("  Skipping seller — login failed.")
            continue

        cats = get_categories(session)
        if not cats:
            print("  Could not fetch categories! Skipping.")
            continue
        print(f"  Categories found: {list(cats.keys())}")

        for i, (title, desc, price, qty, cond, pexels_q) in enumerate(products, 1):
            cat_id = pick_category(cats, title)
            img_url = get_pexels_image_url(pexels_q)

            ok = create_product(session, cat_id, title, desc, price, qty, cond, img_url)
            status = "OK" if ok else "FAIL"
            if ok:
                results["success"] += 1
            else:
                results["failed"] += 1
            print(f"  [{i:02d}/{len(products)}] {status}  {title[:55]}"
                  + (f"  [img: {img_url[:40]}...]" if img_url else "  [no img]"))
            time.sleep(0.4)   # be gentle on the server

    print(f"\n{'='*60}")
    print(f"DONE — {results['success']} succeeded, {results['failed']} failed out of 100")
    print(f"{'='*60}")


if __name__ == "__main__":
    run()
