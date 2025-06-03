To add a `README.md` file to your GitHub repository and push it to the `main` branch, follow these steps:

---

### **Step 1: Create a `README.md` file locally**
1. Navigate to your project folder:
   ```bash
   cd /Users/chamithshaminda/Desktop/w2083586/mobilw2ndcw/
   ```
2. Create the `README.md` file:
   ```bash
   touch README.md
   ```
3. Open the file in a text editor (e.g., VS Code, Nano, or Xcode) and add content:
   ```bash
   echo "# 2D Dice Game for Android" >> README.md
   echo "A simple Android dice-rolling game built with Kotlin." >> README.md
   ```
   Or edit manually:
   ```bash
   nano README.md  # (Ctrl+X → Y → Enter to save)
   ```

---

### **Step 2: Commit the `README.md` file**
1. Stage the file:
   ```bash
   git add README.md
   ```
2. Commit it:
   ```bash
   git commit -m "Add README.md"
   ```

---

### **Step 3: Push to GitHub (main branch)**
```bash
git push origin main
```

---

### **Step 4: Verify on GitHub**
1. Go to your repo:  
   [https://github.com/chamithsilva10/2D-Dice-Game-for-Android](https://github.com/chamithsilva10/2D-Dice-Game-for-Android)
2. The `README.md` should now appear at the bottom of the repository page.

---


Example:
```markdown
# 2D Dice Game for Android 🎲

A simple Android dice-rolling game built with Kotlin.

## Features
- Roll two dice with a button tap
- Simple animations
- Score tracking

## Installation
1. Clone the repo:
   ```bash
   git clone https://github.com/chamithsilva10/2D-Dice-Game-for-Android.git
   ```
2. Open in Android Studio and run!
