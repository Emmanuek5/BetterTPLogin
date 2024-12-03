# BetterTPLogin  
A secure Minecraft authentication plugin with advanced features and user-friendly design.

---

## Key Features

### Authentication
- Secure registration with SHA-256 password hashing  
- Login verification system  
- IP-based auto-login for returning players  
- Password reset functionality  

### Security
- Random spawn point generation (4-block radius)  
- Movement restriction for non-authenticated users  
- Block interaction prevention  
- Anti-grief protection  

### Data Management
- YAML-based persistent storage  
- Location tracking and restoration  
- IP address management  
- Session handling  

---

## Commands
- `/register <password>` - Create new account  
- `/login <password>` - Authenticate existing account  
- `/resetpassword <current> <new>` - Change password  
- `/setspawn` - Set spawn point (Admin only)  

---

## User Experience
- Colored message formatting  
- Automatic spawn point randomization  
- Last location restoration  
- Clear login/register prompts  

---

## Technical Details

### Storage Format
```yaml
UUID:
  location:
    world: world_name
    x: 0.0
    y: 64.0
    z: 0.0
    yaw: 0.0
    pitch: 0.0
  passwordHash: hash_string
  ipAddress: ip_string
