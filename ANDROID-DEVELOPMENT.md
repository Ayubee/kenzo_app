# Kenzo App: kompyuterda sinash

Loyiha Kotlin/Compose va Gradle wrapper ishlatadi. Debug paket nomi:
`com.example.kunlikvazifalar`. Bir xil AVD va debug signing key bilan ishlang.

## Kundalik ishga tushirish

PowerShell terminalida:

```powershell
cd D:\kenzo_app
powershell -NoProfile -ExecutionPolicy Bypass -File .\run-android.ps1
```

Bu buyruq tanlangan emulatorni ochadi (ochiq bo'lsa qayta ishlatadi), Android
yuklanishini kutadi, `:app:assembleDebug` bajaradi, APK'ni `adb install -r` bilan
yangilaydi va ilovani ochadi. Muhim o'zgarishlardan so'ng shu buyruqni takrorlang.

Faqat emulatorni ochish:

```powershell
.\run-android.ps1 -EmulatorOnly
```

Execution policy skriptni to'ssa, yuqoridagi to'liq `powershell ... -File`
buyrug'iga `-EmulatorOnly` qo'shing. Skript execution policy'ni doimiy o'zgartirmaydi.

## Bir martalik sozlash

Windows Hypervisor Platform yoqilgan bo'lishi kerak. Windows restart talab qilsa,
ochiq ishlaringizni saqlab kompyuterni qayta ishga tushiring. Skript Windows'ni
qayta ishga tushirmaydi va tizim komponentlarini o'zgartirmaydi.

Mavjud qurilma bo'lmasa:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\run-android.ps1 -Setup
```

Yuklab, qurilmani yaratish bilan cheklanish uchun `-Setup -PrepareOnly` ishlating.
O'rnatilgan Android CLI (`%USERPROFILE%\.android\bin\android-cli.exe`) kerak.
CLI bo'lmagan boshqa kompyuterda Android SDK Manager orqali Android Emulator,
SDK Command-line Tools va **bitta** Android 16 / API 36 x86_64 tizim obrazini
o'rnating va AVD yarating. Gradle uchun SDK yo'lini `local.properties` ichidagi
`sdk.dir` belgilaydi.

Joriy kompyuter uchun tanlov: **Kenzo_API_36**, Android 16 (API 36), AOSP x86_64,
4 GB RAM, 4 CPU oqimi, 1080 x 1920 ekran, 420 dpi. Google Play bu oflayn ilova
uchun kerak emas. Emulator SDK paketlari va AVD ma'lumotlari
`D:\kenzo_app\.local\android` ichida saqlanadi va Git'ga kiritilmaydi. Mavjud
build SDK qayta o'rnatilmaydi. Bir nechta mavjud AVD bo'lsa, `-AvdName NOM`
bilan bittasini tanlang; skript qo'shimcha qurilma yaratmaydi.

## Ma'lumotlarni saqlash

Oddiy yangilash `adb install -r` bilan bajariladi. Ilova to'xtatilib qayta ochiladi,
lekin uning ma'lumotlari o'chirilmaydi. Username SharedPreferences'da,
vazifalar SQLite bazasida saqlanadi. Shu AVD'ni va foydalanuvchining mavjud debug
signing key'ini saqlang. Baza tuzilishi o'zgarsa, ma'lumotlarni saqlaydigan
migratsiya kerak; o'rnatish usulining o'zi noto'g'ri migratsiyadan himoya qilmaydi.

`uninstall`, `pm clear`, emulator `-wipe-data`, AVD'ni qayta yaratish yoki
`.local/android` katalogini o'chirish odatiy yangilash jarayoniga kirmaydi.
Signature yoki version xatosida skript to'xtaydi, ma'lumotlarni o'chirishga o'tmaydi.
Release APK'ni debug ustiga o'rnatmang: signing key farqi bo'lishi mumkin.

## Tekshirish va release

2026-10-07 kuni amalda tasdiqlangan natijalar:

- Avval emulator va AVD yo'q edi. Bitta `Kenzo_API_36` yaratildi va ochildi;
  `adb devices -l` faqat `emulator-5554` qurilmasini ko'rsatdi.
- Windows Hypervisor Platform yoqildi. Windows restart so'radi, lekin avtomatik
  restart qilinmadi; `emulator -accel-check` WHPX ishlashini tasdiqladi va Android
  muvaffaqiyatli yuklandi.
- `:app:assembleDebug` muvaffaqiyatli yakunlandi; `install -r` va MainActivity'ni
  ochish muvaffaqiyatli bajarildi. Takroriy skript chaqiruvi shu ochiq AVD'ni ishlatdi.
- Onboarding, bildirishnoma ruxsati va vazifa qo'shish UI orqali bajarildi.
  Sinov profili: `Kenzo`; qoldirilgan sinov vazifasi: `Emulator sinovi`.
- Skript bilan qayta o'rnatilgach username va vazifa ekranda saqlandi.
  SharedPreferences XML va SQLite bazasining SHA-256 xeshlari o'zgarmadi.
- Onboarding va bugungi vazifalar ekranlari screenshot orqali ko'rildi.
  Tekshiruv paytida Android crash logi bo'sh edi.

Vazifa tahrirlash/o'chirish/bajarish, tarix, sozlamalar, vaqtli eslatmaning kelishi
va to'liq dizayn tekshiruvi bu sessiyada bajarilmadi. Yakuniy release APK yaratilmadi.
Dalillar `.local/android/before-update-*`, `after-update-*` va `current.png` ichida.

Emulatorda quyidagilarni amalda tekshiring:

- Username kiritish va qayta ochilganda saqlanishi.
- Vazifa qo'shish, tahrirlash, bajarilgan deb belgilash va o'chirish.
- Bugungi vazifalar, tarix, sozlamalar va klaviatura ochilgandagi dizayn.
- Username va vazifa mavjud paytda skriptni qayta bajarib, ikkalasi saqlanishi.
- Vaqtli vazifa eslatmasi va bildirishnoma ruxsatining haqiqiy ishlashi.

Tekshirilmagan bandlarni o'tgan deb belgilamang. Yakuniy release APK faqat dizayn
va asosiy funksiyalar amalda tekshirilgandan keyin tayyorlanadi. Kundalik skript
faqat debug build yaratadi.

Muammo bo'lsa: `.local/android/emulator.stdout.log`,
`.local/android/emulator.stderr.log` va `adb -s SERIAL logcat` ni ko'ring.

Manbalar: [emulator buyruqlari](https://developer.android.com/studio/run/emulator-commandline),
[Windows tezlashtirish](https://developer.android.com/studio/run/emulator-acceleration),
[ADB orqali yangilash](https://developer.android.com/tools/adb#pm).
