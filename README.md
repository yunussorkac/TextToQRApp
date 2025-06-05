![Screenshot_20250605_132353-imageonline co-merged](https://github.com/user-attachments/assets/8a691441-3687-4523-bc21-84cf40ba68d1)

## 🚀 Features

- ✍️ Convert user-entered text into QR codes  
- 🧩 **Handles unlimited-length text** by automatically splitting it into multiple QR codes when necessary  
- 💾 Save generated QR codes locally using Room database  
- 📜 Display a list of previously created QR codes  
- 👇 View QR content via a Modal Bottom Sheet  
- 🖼️ Select QR codes from gallery and decode their contents  
- ⚡ Fast, smooth, and minimal UI with Jetpack Compose

---

## 🧰 Tech Stack

| Technology | Usage |
|------------|-------|
| **Kotlin** | Primary programming language |
| **Jetpack Compose** | UI toolkit for building native Android interfaces |
| **Room** | Local database for storing QR history |
| **ZXing** | QR code generation and decoding |
| **Koin** | Dependency injection framework |
| **Flow** | Asynchronous data handling |
| **MVVM Architecture** | Scalable, testable architecture pattern |

---

- If the input exceeds the capacity of a single QR code, the app **automatically splits** the content into multiple parts.
- Each part is encoded into a separate QR code, maintaining sequence and integrity.
- Upon scanning, users can still retrieve the full content seamlessly.
