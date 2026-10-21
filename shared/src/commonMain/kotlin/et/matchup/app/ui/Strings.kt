package et.matchup.app.ui

import androidx.compose.runtime.staticCompositionLocalOf

/**
 * All user-facing English copy lives here so an Amharic implementation can be
 * supplied later without touching screen code.
 */
interface Strings {
    val appName: String
    val tagline: String
    val back: String
    val close: String
    val cancel: String
    val done: String
    val save: String
    val retry: String
    val refresh: String
    val copy: String
    val copied: String
    val optional: String
    val loading: String
    val delete: String
    val add: String
    val edit: String
    val remove: String
    val search: String
    val clear: String
    val all: String
    val errorGeneric: String
    val errorNetwork: String
    val errorUnauthorized: String
    val fieldRequired: String
    val emailRequired: String
    val passwordRequired: String
    val currentPasswordRequired: String
    val newPasswordRequired: String
    val pleaseFixFields: String
    val offlineWriteBody: String
    val deleteTeammate: String
    val checkInQr: String
    val copyReference: String
    val paymentDetails: String
    val expectedAmount: String
    val submittedAt: String
    val status: String
    val method: String
    val open: String
    val viewDetails: String
    val selectMethod: String
    val noPaymentMethods: String
    val homeSubtitle: String
    val couldNotRefresh: String
    val offlineTitle: String
    val offlineBody: String
    val savedDataNotice: String
    val noConnectionTitle: String
    val noConnectionBody: String
    val versionLabel: String

    val goodMorning: String
    val goodAfternoon: String
    val goodEvening: String
    fun greeting(name: String, timeOfDay: String): String

    val welcomeBack: String
    val signInSubtitle: String
    val email: String
    val password: String
    val fullName: String
    val phone: String
    val phoneHelper: String
    val forgotPassword: String
    val signIn: String
    val signingIn: String
    val or: String
    val continueWithGoogle: String
    val googleSigningIn: String
    val googleNotConfigured: String
    val googleUnavailable: String
    val googleCancelled: String
    val googleNoAccount: String
    val googleFailed: String
    val noAccount: String
    val createAccount: String
    val haveAccount: String
    val createAccountSubtitle: String
    val signUp: String
    val signingUp: String
    val passwordHint: String
    val passwordStrengthWeak: String
    val passwordStrengthFair: String
    val passwordStrengthGood: String
    val passwordStrengthStrong: String
    val invalidEmail: String
    val passwordTooShort: String
    val passwordNeedsMix: String
    val passwordTooLong: String
    val nameTooShort: String
    val phoneTooLong: String
    val showPassword: String
    val hidePassword: String
    val authTerms: String
    val resetPassword: String
    val resetSubtitle: String
    val sendResetLink: String
    val sending: String
    val resetLinkSent: String
    val resetLinkSentBody: String
    val resetToken: String
    val newPassword: String
    val confirmPassword: String
    val updatePassword: String
    val updating: String
    val passwordChanged: String
    val backToSignIn: String
    val resetTokenRequired: String
    val passwordsDontMatch: String

    val discover: String
    val addisAbaba: String
    val searchPlaceholder: String
    val filters: String
    val date: String
    val format: String
    val field: String
    val availableOnly: String
    val clearFilters: String
    val today: String
    val tomorrow: String
    val thisWeek: String
    val allDates: String
    val allFormats: String
    val allFields: String
    val fiveASide: String
    val sevenASide: String
    fun upcomingGames(count: Int): String
    val noGamesTitle: String
    val noGamesBody: String
    val noFilterResultsTitle: String
    val noFilterResultsBody: String
    val full: String
    fun spotsLeft(count: Int): String
    val perPlayer: String
    val gameDetails: String
    val aboutGame: String
    val venue: String
    val dateAndTime: String
    val duration: String
    fun minutes(count: Int): String
    val capacity: String
    val spotsRemaining: String
    val pricePerPlayer: String
    val bookingType: String
    val singleGame: String
    val monthlyPlan: String
    val monthlyPlanHint: String
    val spots: String
    fun spotLabel(index: Int): String
    val teammates: String
    val nameYourSpots: String
    val teammateName: String
    val addSavedTeammate: String
    val savedTeammates: String
    val noSavedTeammates: String
    val reserve: String
    val reserving: String
    val reserveSpots: String
    val reviewHoldTitle: String
    val reviewHoldSubtitle: String
    val confirmAndPay: String
    val continueToPayment: String
    val howItWorksTitle: String
    val howItWorksBody: String
    val total: String
    val subtotal: String
    val discount: String
    val quoteLoading: String
    val offlineBookingBody: String
    val holdNote: String
    val monthlyUnavailable: String
    val teamNamesOptional: String
    val weeklySessions: String
    val gameFullBody: String
    val clearSearch: String
    val openNotifications: String

    val completePayment: String
    val paymentSubtitle: String
    val currency: String
    val reservedFor: String
    val holdExpired: String
    val holdExpiredBody: String
    val expiresIn: String
    val bookAgain: String
    val timelineReserved: String
    val timelineSubmitted: String
    val timelineVerified: String
    val timelineConfirmed: String
    val amountDue: String
    val stepSend: String
    val stepSubmit: String
    val paymentMethods: String
    val accountName: String
    val accountNumber: String
    val instructions: String
    val copyAccount: String
    val paymentReference: String
    val paymentReferenceHint: String
    val accountSuffix: String
    val accountSuffixHint: String
    val submitPayment: String
    val submitting: String
    val paymentSubmitted: String
    val paymentSubmittedBody: String
    val paymentUnderReviewBody: String
    val paymentConfirmedBody: String
    val paymentSafety: String
    val paymentUnavailable: String
    val paymentUnavailableBody: String
    val referenceRequired: String
    val suffixInvalid: String
    val verifyingPayment: String
    val referenceCode: String

    val myBookings: String
    val bookingsTab: String
    val bookingsSubtitle: String
    val upcoming: String
    val past: String
    val active: String
    val history: String
    val noBookings: String
    val noBookingsBody: String
    val payNow: String
    val viewBooking: String
    val cancelReservation: String
    val cancelConfirmTitle: String
    val cancelConfirmBody: String
    val cancelKeep: String
    val cancelConfirm: String
    val cancelling: String
    val cancelUnsupported: String
    val bookingExpired: String
    val bookingConfirmed: String
    val bookingCancelled: String
    val bookingVerifying: String
    val bookingPendingReview: String
    val bookingPendingPayment: String
    val checkInPass: String
    val showAtField: String
    val checkinNote: String
    val referenceCopied: String
    val gamesInPlan: String
    val paymentHistory: String
    val noPayments: String
    val participants: String
    val supportNote: String
    val bookingNotFound: String
    val verificationCheckingTitle: String
    val verificationCheckingBody: String
    val verificationConfirmedTitle: String
    val verificationConfirmedBody: String
    val verificationReviewTitle: String
    val verificationReviewBody: String
    val verificationExpiredTitle: String
    val verificationExpiredBody: String
    val verificationFailedTitle: String
    val verificationFailedBody: String
    val backToDiscover: String
    val share: String
    val addToCalendar: String
    val directions: String
    val leavePaymentTitle: String
    val leavePaymentBody: String
    val keepPaying: String
    val leaveAnyway: String
    val monthsShort: List<String>
    val weekdaysShort: List<String>
    val timeTba: String
    val am: String
    val pm: String
    val dateTba: String
    val justNow: String
    val yesterday: String
    fun minutesAgo(count: Int): String
    fun hoursAgo(count: Int): String
    fun daysAgo(count: Int): String
    val savedJustNow: String
    fun savedMinutesAgo(count: Int): String
    fun savedHoursAgo(count: Int): String
    fun savedDaysAgo(count: Int): String
    fun spotWord(count: Int): String
    val emailPlaceholder: String
    val passwordPlaceholder: String
    val fullNamePlaceholder: String
    val phonePlaceholder: String
    val passwordMinPlaceholder: String
    val repeatPasswordPlaceholder: String
    val paymentReferencePlaceholder: String

    val notifications: String
    val markAllRead: String
    val allRead: String
    val unread: String
    val noNotifications: String
    val noNotificationsBody: String
    val noUnreadNotifications: String
    val notificationFallback: String

    val profile: String
    val editProfile: String
    val changePassword: String
    val signOut: String
    val signOutEverywhere: String
    val emailVerified: String
    val emailNotVerified: String
    val verifyEmail: String
    val account: String
    val accountSubtitle: String
    val profileSaved: String
    val profileSaveUnsupported: String
    val emailVerificationTitle: String
    val emailVerificationBody: String
    val sendVerificationEmail: String
    val verificationSent: String
    val verificationUnsupported: String
    val verificationToken: String
    val verify: String
    val verificationSuccess: String
    val changePasswordSubtitle: String
    val currentPassword: String
    val confirmNewPassword: String
    val updatePasswordButton: String
    val passwordUpdated: String
    val signOutConfirmTitle: String
    val signOutConfirmBody: String
    val signOutEverywhereConfirmTitle: String
    val signOutEverywhereConfirmBody: String
    val offlineData: String
    val offlineDataBody: String
    val clearCachedData: String
    val cacheCleared: String
    val appInfo: String
    val apiServer: String
    val platformLabel: String
    val platformWeb: String
    val platformAndroid: String
    val googleAccount: String
    val savedTeammatesSubtitle: String
    val addTeammate: String
    val teammateAdded: String
    val teammateNamePlaceholder: String
    val noSavedTeammatesBody: String
    val profilePhoto: String
    val deleteAccountHint: String
    val signedOut: String
    val savedTeammatesTitle: String
    val fullNameRequired: String
    val confirmSignOut: String
    val goBack: String
}

object EnglishStrings : Strings {
    override val appName = "MatchUp"
    override val tagline = "Find your game. Book your spot."
    override val back = "Back"
    override val close = "Close"
    override val cancel = "Cancel"
    override val done = "Done"
    override val save = "Save"
    override val retry = "Try again"
    override val refresh = "Refresh"
    override val copy = "Copy"
    override val copied = "Copied"
    override val optional = "Optional"
    override val loading = "Loading"
    override val delete = "Delete"
    override val add = "Add"
    override val edit = "Edit"
    override val remove = "Remove"
    override val search = "Search"
    override val clear = "Clear"
    override val all = "All"
    override val errorGeneric = "Something went wrong. Please try again."
    override val errorNetwork = "Can't reach MatchUp. Check your connection and try again."
    override val errorUnauthorized = "Your session expired. Please sign in again."
    override val fieldRequired = "This field is required."
    override val emailRequired = "Enter your email address."
    override val passwordRequired = "Enter your password."
    override val currentPasswordRequired = "Enter your current password."
    override val newPasswordRequired = "Enter a new password."
    override val pleaseFixFields = "Check the highlighted fields."
    override val offlineWriteBody = "Connect to the internet to make this change."
    override val deleteTeammate = "Delete saved teammate"
    override val checkInQr = "Check-in QR code"
    override val copyReference = "Copy reference"
    override val paymentDetails = "Payment details"
    override val expectedAmount = "Expected amount"
    override val submittedAt = "Submitted"
    override val status = "Status"
    override val method = "Method"
    override val open = "Open"
    override val viewDetails = "View details"
    override val selectMethod = "Select a payment method"
    override val noPaymentMethods = "No payment methods are available right now."
    override val homeSubtitle = "Pick a pitch. Bring your people."
    override val couldNotRefresh = "Couldn't refresh"
    override val offlineTitle = "You're offline"
    override val offlineBody = "Showing saved data. Connect to refresh and book."
    override val savedDataNotice = "Showing saved data from %s."
    override val noConnectionTitle = "No connection"
    override val noConnectionBody = "This page needs a connection the first time it loads."
    override val versionLabel = "Version %s"

    override val goodMorning = "Good morning"
    override val goodAfternoon = "Good afternoon"
    override val goodEvening = "Good evening"
    override fun greeting(name: String, timeOfDay: String) = "$timeOfDay, $name"

    override val welcomeBack = "Welcome back"
    override val signInSubtitle = "Your next game is waiting."
    override val email = "Email"
    override val password = "Password"
    override val fullName = "Full name"
    override val phone = "Phone"
    override val phoneHelper = "Used only so the organiser can reach you about a booking."
    override val forgotPassword = "Forgot password?"
    override val signIn = "Sign in"
    override val signingIn = "Signing in…"
    override val or = "or"
    override val continueWithGoogle = "Continue with Google"
    override val googleSigningIn = "Opening Google…"
    override val googleNotConfigured = "Google sign-in isn't set up in this build yet."
    override val googleUnavailable = "Google sign-in is unavailable right now. Use email sign-in instead."
    override val googleCancelled = "Google sign-in was cancelled."
    override val googleNoAccount = "Google didn't return a usable account. Try another sign-in method."
    override val googleFailed = "Google sign-in couldn't be completed. Please try again."
    override val noAccount = "New to MatchUp?"
    override val createAccount = "Create an account"
    override val haveAccount = "Already have an account?"
    override val createAccountSubtitle = "Create your player profile in under a minute."
    override val signUp = "Create account"
    override val signingUp = "Creating account…"
    override val passwordHint = "At least 8 characters, with a letter and a number or symbol."
    override val passwordStrengthWeak = "Weak"
    override val passwordStrengthFair = "Fair"
    override val passwordStrengthGood = "Good"
    override val passwordStrengthStrong = "Strong"
    override val invalidEmail = "Enter a valid email address."
    override val passwordTooShort = "Password must be at least 8 characters."
    override val passwordNeedsMix = "Include a letter and at least one number or symbol."
    override val passwordTooLong = "Password must be 72 bytes or fewer."
    override val nameTooShort = "Enter your full name."
    override val phoneTooLong = "Phone number is too long."
    override val showPassword = "Show password"
    override val hidePassword = "Hide password"
    override val authTerms = "By continuing you agree to play fair and respect the pitch."
    override val resetPassword = "Reset password"
    override val resetSubtitle = "We'll email you a secure reset link."
    override val sendResetLink = "Send reset link"
    override val sending = "Sending…"
    override val resetLinkSent = "Check your email"
    override val resetLinkSentBody = "If that address is registered, a reset link is on its way. You can also paste the token below."
    override val resetToken = "Reset token"
    override val newPassword = "New password"
    override val confirmPassword = "Confirm password"
    override val updatePassword = "Update password"
    override val updating = "Updating…"
    override val passwordChanged = "Password changed. Sign in with your new password."
    override val backToSignIn = "Back to sign in"
    override val resetTokenRequired = "Paste the token from the reset email."
    override val passwordsDontMatch = "Passwords don't match."

    override val discover = "Discover"
    override val addisAbaba = "Addis Ababa"
    override val searchPlaceholder = "Search fields, locations, formats"
    override val filters = "Filters"
    override val date = "Date"
    override val format = "Format"
    override val field = "Field"
    override val availableOnly = "Available only"
    override val clearFilters = "Clear filters"
    override val today = "Today"
    override val tomorrow = "Tomorrow"
    override val thisWeek = "This week"
    override val allDates = "All dates"
    override val allFormats = "All formats"
    override val allFields = "All fields"
    override val fiveASide = "5-a-side"
    override val sevenASide = "7-a-side"
    override fun upcomingGames(count: Int) = if (count == 1) "1 game" else "$count games"
    override val noGamesTitle = "No games scheduled yet"
    override val noGamesBody = "New games will appear here as soon as they're posted."
    override val noFilterResultsTitle = "No games match those filters"
    override val noFilterResultsBody = "Try a different day, field, or search."
    override val full = "Full"
    override fun spotsLeft(count: Int) = if (count == 1) "1 spot left" else "$count spots left"
    override val perPlayer = "per player"
    override val gameDetails = "Game details"
    override val aboutGame = "About this game"
    override val venue = "Venue"
    override val dateAndTime = "Date & time"
    override val duration = "Duration"
    override fun minutes(count: Int) = "$count min"
    override val capacity = "Capacity"
    override val spotsRemaining = "Spots remaining"
    override val pricePerPlayer = "Price per player"
    override val bookingType = "Booking type"
    override val singleGame = "Single game"
    override val monthlyPlan = "Monthly plan"
    override val monthlyPlanHint = "Four weekly sessions, with a discount."
    override val spots = "Spots"
    override fun spotLabel(index: Int) = "Spot $index"
    override val teammates = "Teammates"
    override val nameYourSpots = "Name your spots"
    override val teammateName = "Teammate name"
    override val addSavedTeammate = "Add a saved teammate"
    override val savedTeammates = "Saved teammates"
    override val noSavedTeammates = "No saved teammates yet"
    override val reserve = "Reserve"
    override val reserving = "Reserving…"
    override val reserveSpots = "Reserve spots"
    override val reviewHoldTitle = "Review & hold"
    override val reviewHoldSubtitle = "Check your spots, then we'll hold them while you pay."
    override val confirmAndPay = "Confirm & pay"
    override val continueToPayment = "Continue"
    override val howItWorksTitle = "How it works"
    override val howItWorksBody = "Pick a game and spots, pay by bank transfer, then show your pass at the field."
    override val total = "Total"
    override val subtotal = "Subtotal"
    override val discount = "Discount"
    override val quoteLoading = "Pricing your selection…"
    override val offlineBookingBody = "Connect to the internet to reserve spots. You can still browse saved games."
    override val holdNote = "Your spots are held for 5 minutes after you reserve."
    override val monthlyUnavailable = "The monthly plan isn't available for this game."
    override val teamNamesOptional = "Optional — leave blank for unnamed spots."
    override val weeklySessions = "weekly sessions"
    override val gameFullBody = "All spots for this game are taken. Check another game or come back later."
    override val clearSearch = "Clear search"
    override val openNotifications = "Open notifications"

    override val completePayment = "Complete payment"
    override val paymentSubtitle = "Send the exact amount, then submit your bank reference."
    override val currency = "ETB"
    override val reservedFor = "Reserved for %s"
    override val holdExpired = "Your hold expired"
    override val holdExpiredBody = "The spots were released. Reserve again to restart the countdown."
    override val expiresIn = "Expires in"
    override val bookAgain = "Book again"
    override val timelineReserved = "Spots reserved"
    override val timelineSubmitted = "Payment submitted"
    override val timelineVerified = "Payment verified"
    override val timelineConfirmed = "Booking confirmed"
    override val amountDue = "Amount due"
    override val stepSend = "1. Send the money"
    override val stepSubmit = "2. Submit your reference"
    override val paymentMethods = "Payment method"
    override val accountName = "Account name"
    override val accountNumber = "Account number"
    override val instructions = "Instructions"
    override val copyAccount = "Copy account number"
    override val paymentReference = "Transaction reference"
    override val paymentReferenceHint = "Paste the receipt link, FT reference, or confirmation code."
    override val accountSuffix = "Account suffix"
    override val accountSuffixHint = "Only needed for a bare CBE FT reference: the last 8 digits of your account."
    override val submitPayment = "Submit payment"
    override val submitting = "Submitting…"
    override val paymentSubmitted = "Payment submitted"
    override val paymentSubmittedBody = "We're checking it with the bank. Your booking will update automatically."
    override val paymentUnderReviewBody = "We're checking your payment with the bank. This screen updates as soon as it clears."
    override val paymentConfirmedBody = "Your payment is confirmed. Your check-in pass is ready."
    override val paymentSafety = "Only send money to the account shown in the app. MatchUp never asks for your PIN or password."
    override val paymentUnavailable = "Payment details unavailable"
    override val paymentUnavailableBody = "The payment accounts could not be loaded. Check your connection and try again."
    override val referenceRequired = "Enter the bank transaction reference."
    override val suffixInvalid = "Enter the 8-digit account suffix."
    override val verifyingPayment = "Verifying payment"
    override val referenceCode = "Reference"

    override val myBookings = "My bookings"
    override val bookingsTab = "Bookings"
    override val bookingsSubtitle = "Your games, payments, and passes in one place."
    override val upcoming = "Upcoming"
    override val past = "Past"
    override val active = "Active"
    override val history = "History"
    override val noBookings = "No bookings yet"
    override val noBookingsBody = "Find a game on Discover and your booking will show up here."
    override val payNow = "Pay now"
    override val viewBooking = "View booking"
    override val cancelReservation = "Cancel reservation"
    override val cancelConfirmTitle = "Cancel this reservation?"
    override val cancelConfirmBody = "Your held spots will be released immediately."
    override val cancelKeep = "Keep reservation"
    override val cancelConfirm = "Cancel reservation"
    override val cancelling = "Cancelling…"
    override val cancelUnsupported = "This server doesn't support app cancellation yet. Contact support to cancel."
    override val bookingExpired = "Expired"
    override val bookingConfirmed = "Confirmed"
    override val bookingCancelled = "Cancelled"
    override val bookingVerifying = "Verifying payment"
    override val bookingPendingReview = "Under review"
    override val bookingPendingPayment = "Awaiting payment"
    override val checkInPass = "Check-in pass"
    override val showAtField = "Show this at the field"
    override val checkinNote = "The venue can scan this pass or use the reference code."
    override val referenceCopied = "Reference copied"
    override val gamesInPlan = "Games in this plan"
    override val paymentHistory = "Payment attempts"
    override val noPayments = "No payments submitted yet."
    override val participants = "Participants"
    override val supportNote = "Need to change a confirmed booking? Contact MatchUp support."
    override val bookingNotFound = "Booking not found"
    override val verificationCheckingTitle = "Checking your payment"
    override val verificationCheckingBody = "This usually takes a moment. You can leave this screen; your booking updates automatically."
    override val verificationConfirmedTitle = "You're in"
    override val verificationConfirmedBody = "Payment confirmed. Your spot is locked in."
    override val verificationReviewTitle = "Payment needs a check"
    override val verificationReviewBody = "We couldn't verify this payment automatically. Support will review it shortly."
    override val verificationExpiredTitle = "This hold expired"
    override val verificationExpiredBody = "The spots were released. Reserve again to restart the countdown."
    override val verificationFailedTitle = "Payment didn't go through"
    override val verificationFailedBody = "Try another payment method, or reserve the spots again."
    override val backToDiscover = "Back to Discover"
    override val share = "Share"
    override val addToCalendar = "Add to calendar"
    override val directions = "Directions"
    override val leavePaymentTitle = "Leave payment?"
    override val leavePaymentBody = "Your hold keeps running. You can finish payment from My bookings."
    override val keepPaying = "Keep paying"
    override val leaveAnyway = "Leave anyway"
    override val monthsShort = listOf(
        "Jan", "Feb", "Mar", "Apr", "May", "Jun",
        "Jul", "Aug", "Sep", "Oct", "Nov", "Dec",
    )
    override val weekdaysShort = listOf(
        "Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun",
    )
    override val timeTba = "Time TBA"
    override val am = "AM"
    override val pm = "PM"
    override val dateTba = "Date TBA"
    override val justNow = "Just now"
    override val yesterday = "Yesterday"
    override fun minutesAgo(count: Int) = "${count}m ago"
    override fun hoursAgo(count: Int) = "${count}h ago"
    override fun daysAgo(count: Int) = "${count}d ago"
    override val savedJustNow = "just now"
    override fun savedMinutesAgo(count: Int) = "$count min ago"
    override fun savedHoursAgo(count: Int) = "$count hr ago"
    override fun savedDaysAgo(count: Int) = "$count days ago"
    override fun spotWord(count: Int) = if (count == 1) "spot" else "spots"
    override val emailPlaceholder = "you@example.com"
    override val passwordPlaceholder = "••••••••••"
    override val fullNamePlaceholder = "Abebe Kebede"
    override val phonePlaceholder = "+251…"
    override val passwordMinPlaceholder = "At least 8 characters"
    override val repeatPasswordPlaceholder = "Repeat your password"
    override val paymentReferencePlaceholder = "FT26123… or receipt link"

    override val notifications = "Notifications"
    override val markAllRead = "Mark all read"
    override val allRead = "All caught up"
    override val unread = "Unread"
    override val noNotifications = "Nothing here yet"
    override val noNotificationsBody = "Booking updates and reminders will appear here."
    override val noUnreadNotifications = "No unread notifications"
    override val notificationFallback = "MatchUp update"

    override val profile = "Profile"
    override val editProfile = "Edit profile"
    override val changePassword = "Change password"
    override val signOut = "Sign out"
    override val signOutEverywhere = "Sign out everywhere"
    override val emailVerified = "Email verified"
    override val emailNotVerified = "Email not verified"
    override val verifyEmail = "Verify email"
    override val account = "Account"
    override val accountSubtitle = "Your player details and security."
    override val profileSaved = "Profile updated"
    override val profileSaveUnsupported = "This server doesn't support profile editing from the app yet."
    override val emailVerificationTitle = "Verify your email"
    override val emailVerificationBody = "A verified email keeps your account recoverable and helps us reach you about bookings."
    override val sendVerificationEmail = "Send verification email"
    override val verificationSent = "Verification email sent"
    override val verificationUnsupported = "This server doesn't support email verification from the app yet."
    override val verificationToken = "Verification token"
    override val verify = "Verify"
    override val verificationSuccess = "Email verified"
    override val changePasswordSubtitle = "Other active sessions will be signed out."
    override val currentPassword = "Current password"
    override val confirmNewPassword = "Confirm new password"
    override val updatePasswordButton = "Update password"
    override val passwordUpdated = "Password updated"
    override val signOutConfirmTitle = "Sign out?"
    override val signOutConfirmBody = "Your saved offline data stays on this device until you clear it."
    override val signOutEverywhereConfirmTitle = "Sign out on all devices?"
    override val signOutEverywhereConfirmBody = "Every refresh token for this account will be revoked."
    override val offlineData = "Offline data"
    override val offlineDataBody = "Games, bookings, and notifications are saved on this device so you can view them without a connection."
    override val clearCachedData = "Clear saved data"
    override val cacheCleared = "Saved data cleared"
    override val appInfo = "App info"
    override val apiServer = "API"
    override val platformLabel = "Platform"
    override val platformWeb = "Web / PWA"
    override val platformAndroid = "Android"
    override val googleAccount = "Google account"
    override val savedTeammatesSubtitle = "Names you use often, kept on this device."
    override val addTeammate = "Add teammate"
    override val teammateAdded = "Teammate saved"
    override val teammateNamePlaceholder = "Name"
    override val noSavedTeammatesBody = "Add names here to fill booking spots faster."
    override val profilePhoto = "Profile photo"
    override val deleteAccountHint = "Deleting an account isn't available in the app yet."
    override val signedOut = "Signed out"
    override val savedTeammatesTitle = "Saved teammates"
    override val fullNameRequired = "Enter your full name."
    override val confirmSignOut = "Sign out"
    override val goBack = "Go back"
}

val LocalStrings = staticCompositionLocalOf<Strings> { EnglishStrings }
