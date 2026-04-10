package n.learn.mdeditorapp.data.remote

data class RegisterRequest(val email: String, val password: String)
data class LoginRequest(val email: String, val password: String)
data class TokenResponse(val token: String)
data class MessageResponse(val message: String)
