# StickyNote Stub

This subproject contains **stub classes and methods** that mimic the presence of APIs from external libraries or newer JDK versions (e.g., Java 21), in order to allow **compilation under Java 11** without introducing unnecessary or incompatible dependencies.

## Purpose

- Avoid adding full dependencies for APIs only needed at compile time.
- Simulate the existence of classes/methods that are available in newer environments.
- Used strictly as a `compileOnly` dependency and **never included in published artifacts**.
- Optionally used with ShadowJar to **relocate** classes so the final bytecode appears to use the original API.

## ⚠️ Warning

This stub code **does not provide real functionality**. Using it at runtime **will result in errors or undefined behavior** unless the real API is available in the runtime environment.

## Legal Notice

- This project **does not contain any copied or derived code** from external libraries or the JDK.
- Only method signatures and class definitions are included as empty placeholders (stubs).
- This code is for internal use only and **must not be distributed as if it were the original API**.
- It is your responsibility to ensure compliance with the license of any real API being simulated.

## Expected Usage

```groovy
dependencies {
    compileOnly project(':bukkit:bukkit-1.16.5:stickynote')
}
