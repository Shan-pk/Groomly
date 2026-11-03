package com.example.data.model

import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

object GroomlyMarketplaceCatalog {

    val locationHubs: List<LocationHub> = listOf(
        LocationHub("hub_blr", "Indiranagar, Bengaluru", "India", 12.9784, 77.6408),
        LocationHub("hub_bom", "Bandra West, Mumbai", "India", 19.0596, 72.8295),
        LocationHub("hub_dxb", "Downtown Boulevard, Dubai", "UAE", 25.1972, 55.2744),
        LocationHub("hub_lon", "Soho & Mayfair, London", "UK", 51.5136, -0.1365)
    )

    fun calculateHaversineKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
            cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
            sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        val raw = r * c
        return ((raw * 10.0).roundToInt() / 10.0).coerceAtLeast(0.3)
    }

    val initialCategories: List<ServiceCategoryItem> = listOf(
        ServiceCategoryItem("cat_1", "Men's Haircut", "SALON", "MENS", 299),
        ServiceCategoryItem("cat_2", "Women's Haircut", "SALON", "WOMENS", 599),
        ServiceCategoryItem("cat_3", "Hair Styling", "SALON", "UNISEX", 449),
        ServiceCategoryItem("cat_4", "Beard Grooming", "SALON", "MENS", 199),
        ServiceCategoryItem("cat_5", "Hair Colouring", "SALON", "UNISEX", 1299),
        ServiceCategoryItem("cat_6", "Facial", "SALON", "UNISEX", 899),
        ServiceCategoryItem("cat_7", "Cleanup", "SALON", "UNISEX", 499),
        ServiceCategoryItem("cat_8", "Manicure", "SALON", "UNISEX", 499),
        ServiceCategoryItem("cat_9", "Pedicure", "SALON", "UNISEX", 599),
        ServiceCategoryItem("cat_10", "Hair Spa", "SALON", "UNISEX", 999),
        ServiceCategoryItem("cat_11", "Bridal Makeup", "BRIDAL", "WOMENS", 8999),
        ServiceCategoryItem("cat_12", "Groom Makeup", "BRIDAL", "MENS", 3999),
        ServiceCategoryItem("cat_13", "Waxing", "SALON", "WOMENS", 399),
        ServiceCategoryItem("cat_14", "Threading", "SALON", "UNISEX", 99),
        ServiceCategoryItem("cat_15", "Skin Care & Laser", "DERMATOLOGY", "UNISEX", 1499),
        ServiceCategoryItem("cat_16", "Diet & Wellness", "DIETITIAN", "UNISEX", 999)
    )

    val coupons: List<CouponItem> = listOf(
        CouponItem("GROOM100", "Flat ₹100 OFF on Men's & Unisex Salons", 15, 100, 400, "SALON"),
        CouponItem("LIME20", "20% OFF International MNC Launch Offer (Up to ₹250)", 20, 250, 500, "ALL"),
        CouponItem("BYOP18", "Extra 18% OFF Self-Created Custom Haircut Packages", 18, 400, 600, "SALON"),
        CouponItem("BRIDALGLOW", "₹1,000 OFF Bridal & Groom Pre-Wedding Packages", 15, 1000, 3500, "BRIDAL"),
        CouponItem("CLINIC15", "15% OFF Verified Dermatology & Diet Consultations", 15, 400, 1000, "MEDICAL")
    )

    val providers: List<MarketplaceProvider> = listOf(
        MarketplaceProvider(
            id = "prov_royal_gents",
            name = "Royal Gents Salon",
            tagline = "Precision Fades • Self-Created Haircut Packages • Groom Lounge",
            description = "International flagship men's luxury barbershop and grooming studio. Come for a signature haircut and build your own custom pre-charged package with beard sculpting, charcoal detox facial, hair spa, or head massage.",
            vertical = "SALON",
            genderFocus = "MENS",
            address = "100 Feet Road, HAL 2nd Stage, Indiranagar, Bengaluru",
            distanceKm = 0.8,
            latitude = 12.9789,
            longitude = 77.6414,
            rating = 4.95,
            reviewCount = 428,
            openingHours = "09:00 AM - 09:30 PM",
            contactPhone = "+919845011223",
            isVerified = true,
            isOpenNow = true,
            isAvailableToday = true,
            homeServiceAvailable = true,
            cancellationPolicy = "Free cancellation up to 2 hours before slot. 100% pre-charged advance refund to UPI or Wallet.",
            liveQueueCurrentServing = 11,
            liveQueueNextToken = 14,
            estimatedWaitPerPersonMin = 8,
            accentHex = 0xFFCCFF00,
            staff = listOf(
                StaffMemberProfile(
                    id = "staff_ahmed",
                    name = "Ahmed",
                    roleTitle = "Senior Barber",
                    specialty = "Zero Skin Fade & Scissor Crop",
                    rating = 4.96,
                    completedJobs = 1840,
                    experienceYears = 9,
                    priceAddonInr = 50,
                    shiftHours = "09:00 AM - 07:00 PM",
                    breakWindow = "01:30 PM - 02:15 PM",
                    isAvailableToday = true,
                    portfolioHighlights = listOf("High Taper Skin Fade", "Textured French Crop", "Classic Pompadour")
                ),
                StaffMemberProfile(
                    id = "staff_rahul",
                    name = "Rahul",
                    roleTitle = "Hair Stylist",
                    specialty = "Korean Perm, Color & Keratin",
                    rating = 4.89,
                    completedJobs = 1120,
                    experienceYears = 6,
                    priceAddonInr = 0,
                    shiftHours = "10:00 AM - 09:00 PM",
                    breakWindow = "02:30 PM - 03:00 PM",
                    isAvailableToday = true,
                    portfolioHighlights = listOf("Ash Platinum Highlights", "Messy Wolf Cut", "Argan Hair Spa")
                ),
                StaffMemberProfile(
                    id = "staff_sameer",
                    name = "Sameer",
                    roleTitle = "Beard Specialist",
                    specialty = "Hot Towel Razor Sculpt & Facial",
                    rating = 4.93,
                    completedJobs = 1490,
                    experienceYears = 7,
                    priceAddonInr = 0,
                    shiftHours = "11:00 AM - 09:30 PM",
                    breakWindow = "03:30 PM - 04:00 PM",
                    isAvailableToday = true,
                    portfolioHighlights = listOf("Sharp Boxed Beard", "Italian Stubble Lineup", "O3+ Charcoal Detox")
                )
            ),
            services = listOf(
                MarketplaceServiceItem(
                    id = "srv_rg_cut",
                    name = "Executive Skin Fade & Wash (Base Haircut)",
                    categoryName = "Men's Haircut",
                    description = "Tailored fade or scissor cut with invigorating scalp wash and matte clay styling.",
                    durationMinutes = 35,
                    bufferMinutes = 5,
                    priceInr = 400,
                    discountPriceInr = 350,
                    genderEligibility = "MENS",
                    homeServiceAvailable = true
                ),
                MarketplaceServiceItem(
                    id = "srv_rg_beard",
                    name = "Hot Towel Beard Sculpt & Razor Lineup",
                    categoryName = "Beard Grooming",
                    description = "Aromatic steam towel ritual, straight-razor cheek & neck outline, and organic beard oil.",
                    durationMinutes = 20,
                    bufferMinutes = 5,
                    priceInr = 250,
                    discountPriceInr = 200,
                    genderEligibility = "MENS",
                    homeServiceAvailable = true
                ),
                MarketplaceServiceItem(
                    id = "srv_rg_facial",
                    name = "O3+ Activated Charcoal Detan Facial",
                    categoryName = "Facial",
                    description = "Deep pore blackhead extraction, glycolic peel, and cooling charcoal hydro-mask.",
                    durationMinutes = 35,
                    bufferMinutes = 5,
                    priceInr = 650,
                    discountPriceInr = 450,
                    genderEligibility = "MENS",
                    homeServiceAvailable = true
                ),
                MarketplaceServiceItem(
                    id = "srv_rg_spa",
                    name = "Moroccanoil Anti-Dandruff Hair Spa & Massage",
                    categoryName = "Hair Spa",
                    description = "Warm argan oil steam infusion with 15-minute acupressure head, neck & shoulder massage.",
                    durationMinutes = 30,
                    bufferMinutes = 5,
                    priceInr = 550,
                    discountPriceInr = 399,
                    genderEligibility = "MENS",
                    homeServiceAvailable = true
                ),
                MarketplaceServiceItem(
                    id = "srv_rg_cleanup",
                    name = "Express Vitamin-C Face Cleanup & Under-Eye Detox",
                    categoryName = "Cleanup",
                    description = "15-minute instant brightness scrub, steam cleanup, and ice-globe under-eye depuffing.",
                    durationMinutes = 20,
                    bufferMinutes = 5,
                    priceInr = 350,
                    discountPriceInr = 250,
                    genderEligibility = "MENS",
                    homeServiceAvailable = true
                ),
                MarketplaceServiceItem(
                    id = "srv_rg_pkg",
                    name = "Ready-Made Premium Grooming Package",
                    categoryName = "Men's Haircut",
                    description = "Pre-curated bundle: Precision Haircut + Beard Trim + Charcoal Glow Facial + Hair Wash.",
                    durationMinutes = 75,
                    bufferMinutes = 10,
                    priceInr = 750,
                    discountPriceInr = 500,
                    genderEligibility = "MENS",
                    homeServiceAvailable = true,
                    isBundle = true,
                    bundleIncludedItems = listOf("Haircut", "Beard trim", "Facial", "Hair wash")
                ),
                MarketplaceServiceItem(
                    id = "srv_rg_groom",
                    name = "Royal Groom Wedding Prep Suite",
                    categoryName = "Groom Makeup",
                    description = "HD natural airbrush groom makeup, O3+ bridal glow facial, haircut, and beard styling.",
                    durationMinutes = 120,
                    bufferMinutes = 15,
                    priceInr = 4999,
                    discountPriceInr = 3999,
                    genderEligibility = "MENS",
                    homeServiceAvailable = true,
                    isBundle = true,
                    bundleIncludedItems = listOf("O3+ Bridal Facial", "HD Airbrush Groom Makeup", "Signature Hair & Beard")
                )
            ),
            offers = listOf(
                "Self-Created Package: Combine Haircut + 2 Add-Ons for automatic 15% OFF",
                "Use GROOM100 for ₹100 OFF • UPI & Razorpay Instant Pre-Charge"
            )
        ),
        MarketplaceProvider(
            id = "prov_velvet_bridal",
            name = "Maison Velvet Beauty & Bridal",
            tagline = "Luxury Women's Parlour • Couture Bridal & Custom Packages",
            description = "Editorial women's beauty parlour and celebrity bridal studio. Build your own custom haircut + facial + manicure/pedicure package with pre-charged priority booking.",
            vertical = "BRIDAL",
            genderFocus = "WOMENS",
            address = "Lavelle Road, Luxury Quarter, Bengaluru",
            distanceKm = 2.1,
            latitude = 12.9719,
            longitude = 77.5993,
            rating = 4.94,
            reviewCount = 615,
            openingHours = "09:30 AM - 08:30 PM",
            contactPhone = "+919845044556",
            isVerified = true,
            isOpenNow = true,
            isAvailableToday = true,
            homeServiceAvailable = true,
            cancellationPolicy = "Free rescheduling up to 4 hours prior. Bridal bookings include complimentary trial consultation.",
            liveQueueCurrentServing = 6,
            liveQueueNextToken = 8,
            estimatedWaitPerPersonMin = 12,
            accentHex = 0xFF8B5CF6,
            staff = listOf(
                StaffMemberProfile(
                    id = "staff_zara",
                    name = "Zara Merchant",
                    roleTitle = "Master Bridal Artist",
                    specialty = "HD Airbrush & Dewy Glass Skin Bridal",
                    rating = 4.98,
                    completedJobs = 940,
                    experienceYears = 11,
                    priceAddonInr = 500,
                    shiftHours = "09:30 AM - 07:30 PM",
                    breakWindow = "02:00 PM - 02:45 PM",
                    isAvailableToday = true,
                    portfolioHighlights = listOf("Sabyasachi Editorial Bridal", "Soft Glam Cocktail Look", "Muhurtham Draping")
                ),
                StaffMemberProfile(
                    id = "staff_meera",
                    name = "Meera Nair",
                    roleTitle = "Senior Hair & Color Director",
                    specialty = "Women's Layer Cut, Balayage & Keratin Spa",
                    rating = 4.91,
                    completedJobs = 1310,
                    experienceYears = 8,
                    priceAddonInr = 0,
                    shiftHours = "10:00 AM - 08:30 PM",
                    breakWindow = "03:00 PM - 03:30 PM",
                    isAvailableToday = true,
                    portfolioHighlights = listOf("Caramel Mocha Balayage", "Butterfly Layers", "Moroccanoil Deep Spa")
                ),
                StaffMemberProfile(
                    id = "staff_ananya",
                    name = "Ananya Rao",
                    roleTitle = "Esthetician & Nail Technician",
                    specialty = "Hydra-Glow Facial, Rica Waxing & Gel Nails",
                    rating = 4.89,
                    completedJobs = 1080,
                    experienceYears = 6,
                    priceAddonInr = 0,
                    shiftHours = "09:30 AM - 08:00 PM",
                    breakWindow = "01:30 PM - 02:00 PM",
                    isAvailableToday = true,
                    portfolioHighlights = listOf("Korean Glass Facial", "Chrome Cat-Eye Manicure", "Rica Brazilian Wax")
                )
            ),
            services = listOf(
                MarketplaceServiceItem(
                    id = "srv_mv_cut",
                    name = "Couture Women's Haircut & Blowdry (Base Cut)",
                    categoryName = "Women's Haircut",
                    description = "Consultation, keratin-infused wash, precision butterfly/layer cut, and bouncy salon blowout.",
                    durationMinutes = 50,
                    bufferMinutes = 10,
                    priceInr = 900,
                    discountPriceInr = 699,
                    genderEligibility = "WOMENS",
                    homeServiceAvailable = true
                ),
                MarketplaceServiceItem(
                    id = "srv_mv_facial",
                    name = "24K Gold & Hyaluronic Glass Facial",
                    categoryName = "Facial",
                    description = "7-step ultrasonic pore cleanup, enzymatic peel, and illuminating hydro-jelly mask.",
                    durationMinutes = 55,
                    bufferMinutes = 10,
                    priceInr = 1600,
                    discountPriceInr = 1199,
                    genderEligibility = "WOMENS",
                    homeServiceAvailable = true
                ),
                MarketplaceServiceItem(
                    id = "srv_mv_wax",
                    name = "Rica White Chocolate Full Arms & Legs Waxing",
                    categoryName = "Waxing",
                    description = "Gentle colophony-free Italian Rica wax with soothing post-wax rose oil.",
                    durationMinutes = 40,
                    bufferMinutes = 5,
                    priceInr = 850,
                    discountPriceInr = 599,
                    genderEligibility = "WOMENS",
                    homeServiceAvailable = true
                ),
                MarketplaceServiceItem(
                    id = "srv_mv_mani_pedi",
                    name = "Luxe Spa Manicure + Pedicure Duo",
                    categoryName = "Manicure",
                    description = "Cuticle care, sea-salt exfoliation, reflexology massage, and gel polish.",
                    durationMinutes = 65,
                    bufferMinutes = 10,
                    priceInr = 1400,
                    discountPriceInr = 999,
                    genderEligibility = "WOMENS",
                    homeServiceAvailable = true,
                    isBundle = true,
                    bundleIncludedItems = listOf("Rose Quartz Manicure", "Heel Peel Pedicure", "Gel Polish")
                ),
                MarketplaceServiceItem(
                    id = "srv_mv_bridal",
                    name = "Signature HD Airbrush Bridal Package",
                    categoryName = "Bridal Makeup",
                    description = "Complete wedding-day luxury package: HD Airbrush Makeup + Couture Hair + Draping + Pre-Bridal Cleanup.",
                    durationMinutes = 180,
                    bufferMinutes = 20,
                    priceInr = 12000,
                    discountPriceInr = 8999,
                    genderEligibility = "WOMENS",
                    homeServiceAvailable = true,
                    isBundle = true,
                    bundleIncludedItems = listOf("HD Airbrush Makeup", "Bridal Hairdo & Florals", "Saree/Lehenga Draping", "Lashes & Lenses")
                )
            ),
            offers = listOf("Use BRIDALGLOW for ₹1,000 OFF Bridal Packages", "Self-Created Package: Up to 20% OFF Multi-Service Combos")
        ),
        MarketplaceProvider(
            id = "prov_monochrome_unisex",
            name = "MONOCHROME Unisex Studio",
            tagline = "Gen-Z Hair Lab • Color Bar • Custom Pre-Charged Combos",
            description = "Minimalist international unisex salon crafted for Gen-Z trendsetters. Mix & match haircuts, ammonia-free hair color, brow threading, and Vitamin-C cleanups.",
            vertical = "SALON",
            genderFocus = "UNISEX",
            address = "Koramangala 4th Block, Bengaluru",
            distanceKm = 2.9,
            latitude = 12.9352,
            longitude = 77.6245,
            rating = 4.88,
            reviewCount = 342,
            openingHours = "10:00 AM - 10:00 PM",
            contactPhone = "+919845077889",
            isVerified = true,
            isOpenNow = true,
            isAvailableToday = true,
            homeServiceAvailable = false,
            cancellationPolicy = "Instant refund on cancellations up to 1 hour before appointment.",
            liveQueueCurrentServing = 19,
            liveQueueNextToken = 21,
            estimatedWaitPerPersonMin = 7,
            accentHex = 0xFFCCFF00,
            staff = listOf(
                StaffMemberProfile(
                    id = "staff_kabir",
                    name = "Kabir Sen",
                    roleTitle = "Creative Hair Director",
                    specialty = "Unisex Editorial Cuts & Vivid Color",
                    rating = 4.90,
                    completedJobs = 875,
                    experienceYears = 7,
                    priceAddonInr = 100,
                    shiftHours = "10:00 AM - 08:00 PM",
                    breakWindow = "02:00 PM - 02:30 PM",
                    isAvailableToday = true,
                    portfolioHighlights = listOf("Electric Copper Mullet", "Textured Shag Cut", "Gloss Keratin")
                ),
                StaffMemberProfile(
                    id = "staff_sia",
                    name = "Sia Kapoor",
                    roleTitle = "Skin & Brow Specialist",
                    specialty = "Threading, Rica Waxing & De-Tan Cleanup",
                    rating = 4.85,
                    completedJobs = 690,
                    experienceYears = 5,
                    priceAddonInr = 0,
                    shiftHours = "11:00 AM - 10:00 PM",
                    breakWindow = "04:00 PM - 04:30 PM",
                    isAvailableToday = true,
                    portfolioHighlights = listOf("Precision Brow Mapping", "Vitamin C Cleanup", "Keratin Hair Spa")
                )
            ),
            services = listOf(
                MarketplaceServiceItem(
                    id = "srv_mc_style",
                    name = "Unisex Restyle Cut & Blowout (Base Cut)",
                    categoryName = "Hair Styling",
                    description = "Gender-neutral restyle haircut with wash, scalp detox, and texture styling.",
                    durationMinutes = 45,
                    bufferMinutes = 5,
                    priceInr = 650,
                    discountPriceInr = 499,
                    genderEligibility = "UNISEX",
                    homeServiceAvailable = false
                ),
                MarketplaceServiceItem(
                    id = "srv_mc_color",
                    name = "Global Ammonia-Free Hair Colouring",
                    categoryName = "Hair Colouring",
                    description = "L'Oréal INOA ammonia-free global shine color with bond-repair treatment.",
                    durationMinutes = 90,
                    bufferMinutes = 15,
                    priceInr = 1999,
                    discountPriceInr = 1499,
                    genderEligibility = "UNISEX",
                    homeServiceAvailable = false
                ),
                MarketplaceServiceItem(
                    id = "srv_mc_cleanup",
                    name = "Express Vitamin-C Cleanup + Threading",
                    categoryName = "Cleanup",
                    description = "Quick 30-minute de-tan cleanup paired with precision eyebrow and upper-lip threading.",
                    durationMinutes = 35,
                    bufferMinutes = 5,
                    priceInr = 650,
                    discountPriceInr = 499,
                    genderEligibility = "UNISEX",
                    homeServiceAvailable = false
                ),
                MarketplaceServiceItem(
                    id = "srv_mc_pedi",
                    name = "Detox Matcha Spa Pedicure",
                    categoryName = "Pedicure",
                    description = "Green tea soak, callus smoothing, and lower-leg lymphatic massage.",
                    durationMinutes = 40,
                    bufferMinutes = 5,
                    priceInr = 750,
                    discountPriceInr = 549,
                    genderEligibility = "UNISEX",
                    homeServiceAvailable = false
                )
            ),
            offers = listOf("Use LIME20 for 20% OFF", "Live Queue Express Walk-In Enabled")
        ),
        MarketplaceProvider(
            id = "prov_aura_derma",
            name = "AURA Dermatology & Skin Clinic",
            tagline = "Board-Certified Dermatologists • Medical Aesthetics",
            description = "HIPAA/DISHA privacy-compliant dermatology and clinical skin-care center. Separate medical verification, encrypted consultation notes, laser toning, acne protocols, and bridal skin prep.",
            vertical = "DERMATOLOGY",
            genderFocus = "UNISEX",
            address = "UB City Medical Suites, Vittal Mallya Road, Bengaluru",
            distanceKm = 3.1,
            latitude = 12.9716,
            longitude = 77.5961,
            rating = 4.97,
            reviewCount = 289,
            openingHours = "10:00 AM - 07:30 PM",
            contactPhone = "+918041129900",
            isVerified = true,
            isOpenNow = true,
            isAvailableToday = true,
            homeServiceAvailable = false,
            medicalComplianceBadge = "NMC Reg #KA-48291 • Encrypted Medical Records",
            cancellationPolicy = "Full refund up to 3 hours prior. Strict patient data confidentiality enforced.",
            liveQueueCurrentServing = 4,
            liveQueueNextToken = 5,
            estimatedWaitPerPersonMin = 15,
            accentHex = 0xFF38BDF8,
            staff = listOf(
                StaffMemberProfile(
                    id = "staff_dr_aarav",
                    name = "Dr. Aarav Mehta, MD",
                    roleTitle = "Chief Consultant Dermatologist",
                    specialty = "Clinical Acne, Pigmentation & Laser Toning",
                    rating = 4.98,
                    completedJobs = 2150,
                    experienceYears = 12,
                    priceAddonInr = 200,
                    shiftHours = "10:00 AM - 06:00 PM",
                    breakWindow = "01:30 PM - 02:30 PM",
                    isAvailableToday = true,
                    portfolioHighlights = listOf("Q-Switch Laser Protocol", "Acne Scar Microneedling", "Pre-Wedding Dermaplaning")
                )
            ),
            services = listOf(
                MarketplaceServiceItem(
                    id = "srv_ad_consult",
                    name = "Dermatologist Clinical Consultation + Dermoscopy",
                    categoryName = "Skin Care & Laser",
                    description = "Comprehensive 30-min skin & scalp diagnostic consultation with digital dermoscopy and prescription plan.",
                    durationMinutes = 30,
                    bufferMinutes = 10,
                    priceInr = 1000,
                    discountPriceInr = 799,
                    genderEligibility = "UNISEX",
                    homeServiceAvailable = false
                ),
                MarketplaceServiceItem(
                    id = "srv_ad_hydra",
                    name = "Medical Grade HydraFacial MD + LED Therapy",
                    categoryName = "Skin Care & Laser",
                    description = "Doctor-supervised vortex salicylic peel, painless extraction, peptide infusion, and dermal LED.",
                    durationMinutes = 60,
                    bufferMinutes = 15,
                    priceInr = 2999,
                    discountPriceInr = 2299,
                    genderEligibility = "UNISEX",
                    homeServiceAvailable = false
                )
            ),
            offers = listOf("Use CLINIC15 for 15% OFF", "Verified Medical Clinic • Private Records")
        ),
        MarketplaceProvider(
            id = "prov_nourish_diet",
            name = "NOURISH Clinical Diet & Wellness",
            tagline = "Certified Nutritionists • Wedding Glow & Metabolic Plans",
            description = "In-clinic and online nutrition consultations for pre-wedding body transformation, PCOS/gut health, skin radiance nutrition, and monthly subscription coaching.",
            vertical = "DIETITIAN",
            genderFocus = "UNISEX",
            address = "HSR Layout Sector 2 & Online Tele-Consult, Bengaluru",
            distanceKm = 4.0,
            latitude = 12.9121,
            longitude = 77.6446,
            rating = 4.93,
            reviewCount = 198,
            openingHours = "08:30 AM - 08:00 PM",
            contactPhone = "+919845099001",
            isVerified = true,
            isOpenNow = true,
            isAvailableToday = true,
            homeServiceAvailable = true,
            medicalComplianceBadge = "IDA Registered Clinical Dietitian • #RD-9021",
            cancellationPolicy = "Free slot reschedule anytime up to 1 hour before online or clinic session.",
            liveQueueCurrentServing = 3,
            liveQueueNextToken = 4,
            estimatedWaitPerPersonMin = 10,
            accentHex = 0xFF34D399,
            staff = listOf(
                StaffMemberProfile(
                    id = "staff_dt_kavya",
                    name = "Dt. Kavya Nair, MSc RD",
                    roleTitle = "Lead Clinical Dietitian",
                    specialty = "Pre-Wedding Glow Diet, Gut Health & Hormonal Balance",
                    rating = 4.95,
                    completedJobs = 1420,
                    experienceYears = 9,
                    priceAddonInr = 0,
                    shiftHours = "09:00 AM - 07:00 PM",
                    breakWindow = "01:00 PM - 02:00 PM",
                    isAvailableToday = true,
                    portfolioHighlights = listOf("90-Day Bridal & Groom Nutrition", "Collagen & Skin Glow Diet", "Metabolic Reset")
                )
            ),
            services = listOf(
                MarketplaceServiceItem(
                    id = "srv_nd_consult",
                    name = "1-on-1 Clinical Nutrition & Body Composition Assessment",
                    categoryName = "Diet & Wellness",
                    description = "45-minute metabolic profiling, blood-work review, and personalized meal plan.",
                    durationMinutes = 45,
                    bufferMinutes = 10,
                    priceInr = 1200,
                    discountPriceInr = 899,
                    genderEligibility = "UNISEX",
                    homeServiceAvailable = true
                ),
                MarketplaceServiceItem(
                    id = "srv_nd_wedding_sub",
                    name = "30-Day Pre-Wedding Glow & Fitness Nutrition Package",
                    categoryName = "Diet & Wellness",
                    description = "4 weekly dietitian check-ins, anti-bloating wedding week protocol, and WhatsApp meal tracking.",
                    durationMinutes = 60,
                    bufferMinutes = 10,
                    priceInr = 3499,
                    discountPriceInr = 2699,
                    genderEligibility = "UNISEX",
                    homeServiceAvailable = true,
                    isBundle = true,
                    bundleIncludedItems = listOf("4 Weekly Consultations", "Custom Macros Plan", "Skin & Hair Micronutrient Guide")
                )
            ),
            offers = listOf("Use CLINIC15 for 15% OFF", "Online & Clinic Consultations Available")
        )
    )

    val standardTimeSlots: List<String> = listOf(
        "09:30 AM", "10:15 AM", "11:00 AM", "11:45 AM",
        "12:30 PM", "02:15 PM", "03:00 PM", "03:45 PM",
        "04:30 PM", "05:15 PM", "06:00 PM", "07:00 PM", "08:00 PM"
    )

    val architectureDeliverables: List<ArchitectureDeliverable> = listOf(
        ArchitectureDeliverable(
            number = 1,
            title = "International MNC HTML5 + Jetpack Compose Hybrid Experience",
            category = "UI/UX & Hybrid Architecture",
            summary = "Animated HTML5/CSS3/JS Main Page connected via bidirectional @JavascriptInterface bridge to native Jetpack Compose, Cloud Firestore, GPS LocationManager, UPI Deep-Links, and WhatsApp.",
            keyHighlights = listOf(
                "Interactive Self-Created Custom Package Builder (Haircut + dynamic shop add-ons with automatic tier discounts)",
                "Real-time Haversine GPS distance calculation & nearest salon radar sorting",
                "Dual-mode Payment Gateway (Android UPI Intent + Razorpay/Stripe Multi-Gateway Checkout)"
            )
        ),
        ArchitectureDeliverable(
            number = 2,
            title = "Smart Appointment Booking & Conflict Locking Engine",
            category = "Core Booking Engine",
            summary = "10-step frictionless booking pipeline calculating dynamic slot availability from service duration, buffer time, staff shift schedules, breaks, and existing appointments.",
            keyHighlights = listOf(
                "Automatic double-booking prevention: booked (providerId + staffId + dateIso + timeSlot) combinations are locked in real time",
                "Self-Created Packaging (BYOP): Customers coming for a haircut can add any shop service and pre-charge 20%, 50%, or 100%",
                "India-first & Global payment breakdown: Service Price -> Pre-Charged Advance -> Remaining Balance at Shop"
            )
        ),
        ArchitectureDeliverable(
            number = 3,
            title = "Real-Time Live Queue & QR Check-In System",
            category = "Live Queue Engine",
            summary = "Digital walk-in token system with real-time snapshot listeners, currently-serving tracker, non-guaranteed wait estimator, and QR check-in code generation.",
            keyHighlights = listOf(
                "Customers join queue remotely, receive token # and QR code (e.g., GRM-Q-14-ROYA), and see live turn progression",
                "Providers advance queue status (WAITING -> APPROACHING -> CHECKED_IN -> SERVING -> COMPLETED)",
                "Clear disclaimer: Estimated waiting times are dynamic indicators and not guaranteed"
            )
        ),
        ArchitectureDeliverable(
            number = 4,
            title = "Normalized Relational & Cloud Schema (26 Core Entities)",
            category = "Database Engineering",
            summary = "Complete relational entity map covering Users, Roles, Providers, Locations (PostGIS), Staff, Schedules, Bundles, Appointments, Queue Tickets, Payments, Settlements, and Audit Logs.",
            keyHighlights = listOf(
                "Zero-Trust ABAC Security Rules verified and deployed to Cloud Firestore",
                "Strict tenant isolation (`resource.data.userId == request.auth.uid`) and terminal state locking",
                "Configurable platform commission (5%-40%), provider SaaS subscriptions, and WhatsApp Business Cloud API dispatch"
            )
        )
    )
}
