package com.example.util

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.unit.LayoutDirection

enum class AppLanguage(val code: String, val displayName: String, val flagEmoji: String, val layoutDirection: LayoutDirection) {
    FR("fr", "Français", "🇫🇷", LayoutDirection.Ltr),
    EN("en", "English", "🇬🇧", LayoutDirection.Ltr),
    AR("ar", "العربية", "🇸🇦", LayoutDirection.Rtl);

    companion object {
        fun fromCode(code: String): AppLanguage {
            return values().firstOrNull { it.code.equals(code, ignoreCase = true) } ?: FR
        }
    }
}

data class AppStrings(
    // App Header
    val appName: String = "PrivaTube",
    val appTagline: String = "YouTube privé pour la famille",
    val parentButton: String = "Parents",
    val activeSpacePrefix: String = "Espace :",
    val allProfilesLabel: String = "Tous les profils",

    // Child Space Tabs
    val tabHome: String = "Accueil",
    val tabFavorites: String = "Favoris",
    val tabCategories: String = "Catégories",

    // Categories
    val categoryAll: String = "Tous",
    val categoryFavorites: String = "Favoris",
    val categoryCartoons: String = "Dessins animés",
    val categoryMusic: String = "Musique",
    val categoryEducational: String = "Éducatif",
    val categoryHumor: String = "Humour",

    // Video Card
    val playVideo: String = "Regarder",
    val noVideosFound: String = "Aucune vidéo disponible dans cette catégorie.",
    val noFavoritesFound: String = "Aucune vidéo favorite pour le moment. Cliquez sur la petite étoile d'une vidéo pour l'ajouter !",

    // Parent Navigation Tabs
    val parentTabDashboard: String = "Tableau de bord",
    val parentTabVideos: String = "Vidéos",
    val parentTabProfiles: String = "Profils",
    val parentTabSettings: String = "Paramètres",

    // Parent Header
    val parentDashboardTitle: String = "Espace Parent",
    val parentDashboardSubtitle: String = "Gestion & Sécurité Familiale",

    // Dashboard Cards
    val totalVideosCard: String = "Vidéos autorisées",
    val totalProfilesCard: String = "Profils enfants",
    val activeProfileCard: String = "Profil actif",
    val familyCodeCardTitle: String = "Code Famille & Sync Cloud",
    val familyCodeCardDesc: String = "Entrez ce code sur vos autres appareils pour synchroniser automatiquement les vidéos à distance.",
    val copyCodeBtn: String = "Copier",
    val joinCodeBtn: String = "Rejoindre un code",
    val newCodeBtn: String = "Nouveau code",

    // Quick Actions
    val quickActionsTitle: String = "Raccourcis rapides",
    val addSingleVideoBtn: String = "+ Vidéo unique",
    val addChannelBtn: String = "📺 Chaîne entière",
    val manageProfilesBtn: String = "👥 Gérer les profils",

    // Video Management Tab
    val searchVideoPlaceholder: String = "Rechercher une vidéo par titre ou description...",
    val filterByCategory: String = "Catégorie :",
    val filterByProfile: String = "Profil attribué :",
    val editVideo: String = "Modifier",
    val deleteVideo: String = "Supprimer",
    val videoAssignedToAll: String = "Tous les enfants",

    // Profile Management Tab
    val profilesTitle: String = "Profils Enfants",
    val profilesSubtitle: String = "Créez un profil pour chaque enfant pour lui attribuer du contenu adapté à son âge.",
    val addProfileBtn: String = "+ Nouveau profil enfant",
    val activeProfileDeviceTitle: String = "Profil actif sur cet appareil",
    val activeProfileDeviceDesc: String = "L'enfant verra uniquement les vidéos attribuées à ce profil.",
    val yearsOld: String = "ans",

    // Profile Dialog
    val createProfileTitle: String = "Créer un profil enfant",
    val editProfileTitle: String = "Modifier le profil",
    val childNameLabel: String = "Prénom de l'enfant",
    val childAgeLabel: String = "Âge",
    val chooseAvatarEmoji: String = "Choisissez un avatar :",
    val saveBtn: String = "Enregistrer",
    val cancelBtn: String = "Annuler",

    // Add Video Dialog
    val addSingleVideoTitle: String = "Ajouter une vidéo YouTube",
    val youtubeUrlOrIdLabel: String = "Lien URL ou ID de la vidéo YouTube",
    val youtubeUrlHint: String = "Ex: https://www.youtube.com/watch?v=...",
    val videoTitleLabel: String = "Titre personnalisé (optionnel)",
    val ageMinLabel: String = "Âge minimum",
    val ageMaxLabel: String = "Âge maximum",
    val targetProfileLabel: String = "Attribuer au profil",
    val addBtn: String = "Ajouter",

    // Add Channel Dialog
    val addChannelTitle: String = "Importer une chaîne YouTube",
    val channelUrlOrIdLabel: String = "ID ou URL de la chaîne / Handle",
    val channelUrlHint: String = "Ex: @NomDeLaChaine ou UC...",
    val maxVideosToFetch: String = "Nombre de vidéos récentes à importer",
    val importBtn: String = "Importer la chaîne",

    // Settings Tab
    val settingsTitle: String = "Paramètres de l'application",
    val sectionGeneral: String = "PARAMÈTRES GÉNÉRAUX",
    val languageLabel: String = "Langue de l'application",
    val languageDesc: String = "Choisissez la langue de l'interface",
    val themeLabel: String = "Apparence / Thème",
    val themeDesc: String = "Clair, Sombre ou Suivre le système",
    val themeSystem: String = "Système",
    val themeLight: String = "Clair",
    val themeDark: String = "Sombre",

    val sectionSecurity: String = "SÉCURITÉ & ACCÈS PARENTAL",
    val pinLabel: String = "Code PIN de sécurité",
    val pinDesc: String = "Code à 4 chiffres requis pour ouvrir l'espace parent",
    val changePinBtn: String = "Changer le PIN",
    val biometricLabel: String = "Déverrouillage biométrique",
    val biometricDesc: String = "Empreinte digitale / Reconnaissance faciale",

    val sectionCloud: String = "SYNCHRONISATION CLOUD & FAMILLE",
    val cloudStatusLabel: String = "Statut de synchronisation Cloud",
    val cloudStatusOnline: String = "En direct & Synchronisé",
    val cloudStatusOffline: String = "Hors ligne",

    val sectionAbout: String = "À PROPOS DE PRIVATUBE",
    val aboutDesc: String = "PrivaTube est un lecteur YouTube sécurisé pour enfants. Les parents ont le contrôle total du contenu autorisé.",
    val appVersionLabel: String = "Version 2.0.0",

    // Add/Edit Video Screen
    val addContentTitle: String = "Ajouter du contenu YouTube",
    val editVideoTitle: String = "Modifier la vidéo",
    val tabSingleVideo: String = "Vidéo unique",
    val tabEntireChannel: String = "Chaîne entière",
    val step1VideoLink: String = "1. Lien de la vidéo",
    val pasteUrlLabel: String = "Coller l'URL de la vidéo",
    val pasteBtn: String = "Coller",
    val thumbnailPreviewLabel: String = "Aperçu de la miniature :",
    val invalidUrlFormat: String = "Format d'URL non reconnu. Assurez-vous d'utiliser un lien YouTube valide.",
    val step2VideoInfo: String = "2. Informations de la vidéo",
    val customTitleLabel: String = "Titre personnalisé pour l'enfant *",
    val customTitleHint: String = "Ex: Petit Ours Brun fait du vélo",
    val categoryLabel: String = "Catégorie",
    val assignProfileLabel: String = "Attribuer au profil enfant",
    val parentNoteLabel: String = "Description / Note parent (Optionnel)",
    val addToLibraryBtn: String = "Ajouter à la bibliothèque",
    val saveChangesBtn: String = "Enregistrer les modifications",
    val channelInputTitle: String = "Lien ou Nom de la chaîne",
    val channelInputDesc: String = "Entrez le nom de la chaîne (ex: @cocomelon, @peppapig) ou l'URL complète de la chaîne YouTube.",
    val channelInputLabel: String = "Nom ou URL de la chaîne",
    val channelInputHint: String = "@cocomelon ou https://youtube.com/@...",
    val analyzingChannelMsg: String = "Analyse de la chaîne...",
    val analyzeChannelBtn: String = "Analyser la chaîne YouTube",
    val channelPrefix: String = "Chaîne :",
    val videosSelectedCount: String = "vidéo(s) sélectionnée(s)",
    val deselectAll: String = "Tout désélectionner",
    val selectAll: String = "Tout sélectionner",
    val channelCategoryLabel: String = "Catégorie d'attribution pour la chaîne",
    val importVideosBtnPrefix: String = "Importer",
    val importSuccessToast: String = "vidéo(s) de la chaîne importée(s) avec succès !",

    // PIN Dialog
    val pinModalDefaultTitle: String = "Espace Parent",
    val pinModalDefaultSubtitle: String = "Saisissez votre code PIN ou utilisez la biométrie",
    val pinIncorrectError: String = "Code PIN incorrect. Réessayez.",
    val biometricButtonLabel: String = "Empreinte / Face ID",
    val closeBtn: String = "Fermer",
    val clearBtn: String = "Effacer",

    // Parent Dashboard extras
    val quickAddTitle: String = "Ajout rapide de contenu",
    val importChannelBtn: String = "📺 Import Chaîne",
    val searchLibraryPlaceholder: String = "Rechercher dans la bibliothèque...",
    val noVideosPrompt: String = "Aucune vidéo. Cliquez sur + Ajouter !",
    val noSearchResults: String = "Aucun résultat pour",
    val deleteVideoConfirmTitle: String = "Supprimer cette vidéo ?",
    val deleteVideoConfirmText: String = "Êtes-vous sûr de vouloir supprimer cette vidéo de la bibliothèque ?",
    val appAboutDesc: String = "Contrôle parental vidéo privé pour enfants avec synchronisation Cloud multi-appareils."
)

val FrenchStrings = AppStrings()

val EnglishStrings = AppStrings(
    appName = "PrivaTube",
    appTagline = "Private YouTube for family",
    parentButton = "Parents",
    activeSpacePrefix = "Space:",
    allProfilesLabel = "All profiles",

    tabHome = "Home",
    tabFavorites = "Favorites",
    tabCategories = "Categories",

    categoryAll = "All",
    categoryFavorites = "Favorites",
    categoryCartoons = "Cartoons",
    categoryMusic = "Music",
    categoryEducational = "Educational",
    categoryHumor = "Humor",

    playVideo = "Watch",
    noVideosFound = "No videos available in this category.",
    noFavoritesFound = "No favorite videos yet. Click the small star on any video to add it!",

    parentTabDashboard = "Dashboard",
    parentTabVideos = "Videos",
    parentTabProfiles = "Profiles",
    parentTabSettings = "Settings",

    parentDashboardTitle = "Parent Space",
    parentDashboardSubtitle = "Family Safety & Content Control",

    totalVideosCard = "Allowed Videos",
    totalProfilesCard = "Child Profiles",
    activeProfileCard = "Active Profile",
    familyCodeCardTitle = "Family Code & Cloud Sync",
    familyCodeCardDesc = "Enter this code on your other devices to automatically sync family videos remotely.",
    copyCodeBtn = "Copy",
    joinCodeBtn = "Join code",
    newCodeBtn = "New code",

    quickActionsTitle = "Quick Shortcuts",
    addSingleVideoBtn = "+ Single Video",
    addChannelBtn = "📺 Entire Channel",
    manageProfilesBtn = "👥 Manage Profiles",

    searchVideoPlaceholder = "Search video by title or description...",
    filterByCategory = "Category:",
    filterByProfile = "Assigned Profile:",
    editVideo = "Edit",
    deleteVideo = "Delete",
    videoAssignedToAll = "All children",

    profilesTitle = "Child Profiles",
    profilesSubtitle = "Create a profile for each child to assign age-appropriate content.",
    addProfileBtn = "+ New Child Profile",
    activeProfileDeviceTitle = "Active profile on this device",
    activeProfileDeviceDesc = "The child will only see videos assigned to this selected profile.",
    yearsOld = "years old",

    createProfileTitle = "Create Child Profile",
    editProfileTitle = "Edit Profile",
    childNameLabel = "Child's First Name",
    childAgeLabel = "Age",
    chooseAvatarEmoji = "Choose an avatar:",
    saveBtn = "Save",
    cancelBtn = "Cancel",

    addSingleVideoTitle = "Add YouTube Video",
    youtubeUrlOrIdLabel = "YouTube Video URL or ID",
    youtubeUrlHint = "E.g. https://www.youtube.com/watch?v=...",
    videoTitleLabel = "Custom title (optional)",
    ageMinLabel = "Min age",
    ageMaxLabel = "Max age",
    targetProfileLabel = "Assign to profile",
    addBtn = "Add",

    addChannelTitle = "Import YouTube Channel",
    channelUrlOrIdLabel = "Channel ID, URL or Handle",
    channelUrlHint = "E.g. @ChannelName or UC...",
    maxVideosToFetch = "Number of recent videos to import",
    importBtn = "Import Channel",

    settingsTitle = "Application Settings",
    sectionGeneral = "GENERAL SETTINGS",
    languageLabel = "App Language",
    languageDesc = "Select user interface language",
    themeLabel = "App Theme",
    themeDesc = "Light, Dark, or System default",
    themeSystem = "System",
    themeLight = "Light",
    themeDark = "Dark",

    sectionSecurity = "SECURITY & PARENT CONTROL",
    pinLabel = "Security PIN Code",
    pinDesc = "4-digit code required to enter parent space",
    changePinBtn = "Change PIN",
    biometricLabel = "Biometric Unlock",
    biometricDesc = "Fingerprint / Face recognition",

    sectionCloud = "CLOUD SYNC & FAMILY",
    cloudStatusLabel = "Cloud Sync Status",
    cloudStatusOnline = "Live & Synced",
    cloudStatusOffline = "Offline",

    sectionAbout = "ABOUT PRIVATUBE",
    aboutDesc = "PrivaTube is a safe YouTube player for kids. Parents have full control over allowed content.",
    appVersionLabel = "Version 2.0.0",

    addContentTitle = "Add YouTube Content",
    editVideoTitle = "Edit Video",
    tabSingleVideo = "Single Video",
    tabEntireChannel = "Entire Channel",
    step1VideoLink = "1. Video Link",
    pasteUrlLabel = "Paste Video URL",
    pasteBtn = "Paste",
    thumbnailPreviewLabel = "Thumbnail Preview:",
    invalidUrlFormat = "Unrecognized URL format. Make sure to use a valid YouTube link.",
    step2VideoInfo = "2. Video Details",
    customTitleLabel = "Custom Title for Child *",
    customTitleHint = "E.g. Mickey Mouse Episode 1",
    categoryLabel = "Category",
    assignProfileLabel = "Assign to Child Profile",
    parentNoteLabel = "Description / Parent Note (Optional)",
    addToLibraryBtn = "Add to Library",
    saveChangesBtn = "Save Changes",
    channelInputTitle = "Channel Link or Name",
    channelInputDesc = "Enter channel name (e.g. @cocomelon, @peppapig) or full YouTube channel URL.",
    channelInputLabel = "Channel Name or URL",
    channelInputHint = "@cocomelon or https://youtube.com/@...",
    analyzingChannelMsg = "Analyzing channel...",
    analyzeChannelBtn = "Analyze YouTube Channel",
    channelPrefix = "Channel:",
    videosSelectedCount = "video(s) selected",
    deselectAll = "Deselect All",
    selectAll = "Select All",
    channelCategoryLabel = "Assigned Category for Channel",
    importVideosBtnPrefix = "Import",
    importSuccessToast = "channel video(s) imported successfully!",

    pinModalDefaultTitle = "Parent Space",
    pinModalDefaultSubtitle = "Enter your PIN code or use biometrics",
    pinIncorrectError = "Incorrect PIN code. Try again.",
    biometricButtonLabel = "Fingerprint / Face ID",
    closeBtn = "Close",
    clearBtn = "Clear",

    quickAddTitle = "Quick Add Content",
    importChannelBtn = "📺 Import Channel",
    searchLibraryPlaceholder = "Search in library...",
    noVideosPrompt = "No videos. Click + Add!",
    noSearchResults = "No results for",
    deleteVideoConfirmTitle = "Delete this video?",
    deleteVideoConfirmText = "Are you sure you want to delete this video from the library?",
    appAboutDesc = "Private video parental control for children with multi-device cloud synchronization."
)

val ArabicStrings = AppStrings(
    appName = "PrivaTube",
    appTagline = "يوتيوب عائلي آمن للأطفال",
    parentButton = "الوالدين",
    activeSpacePrefix = "المساحة :",
    allProfilesLabel = "جميع الملفات",

    tabHome = "الرئيسية",
    tabFavorites = "المفضلة",
    tabCategories = "التصنيفات",

    categoryAll = "الكل",
    categoryFavorites = "المفضلة",
    categoryCartoons = "رسوم متحركة",
    categoryMusic = "موسيقى",
    categoryEducational = "تعليمي",
    categoryHumor = "مرح وترفيه",

    playVideo = "مشاهدة",
    noVideosFound = "لا توجد فيديوهات متاحة في هذا التصنيف.",
    noFavoritesFound = "لا توجد فيديوهات مفضلة حالياً. اضغط على النجمة لإضافة فيديو إلى المفضلة!",

    parentTabDashboard = "لوحة التحكم",
    parentTabVideos = "الفيديوهات",
    parentTabProfiles = "الملفات",
    parentTabSettings = "الإعدادات",

    parentDashboardTitle = "مساحة الوالدين",
    parentDashboardSubtitle = "إدارة المحتوى والأمان العائلي",

    totalVideosCard = "الفيديوهات المسموحة",
    totalProfilesCard = "ملفات الأطفال",
    activeProfileCard = "الملف النشط",
    familyCodeCardTitle = "رمز العائلة والربط السحابي",
    familyCodeCardDesc = "أدخل هذا الرمز على أجهزتك الأخرى لمزامنة فيديوهات العائلة تلقائياً عن بُعد.",
    copyCodeBtn = "نسخ",
    joinCodeBtn = "الانضمام لرمز",
    newCodeBtn = "رمز جديد",

    quickActionsTitle = "اختصارات سريعة",
    addSingleVideoBtn = "+ فيديو منفرد",
    addChannelBtn = "📺 قناة كاملة",
    manageProfilesBtn = "👥 إدارة الملفات",

    searchVideoPlaceholder = "ابحث عن فيديو بالعنوان أو الوصف...",
    filterByCategory = "التصنيف:",
    filterByProfile = "المخصص لـ:",
    editVideo = "تعديل",
    deleteVideo = "حذف",
    videoAssignedToAll = "جميع الأطفال",

    profilesTitle = "ملفات الأطفال الشخصية",
    profilesSubtitle = "أنشئ ملفاً لكل طفل لتخصيص المحتوى المناسب لعمره.",
    addProfileBtn = "+ إنشاء ملف طفل جديد",
    activeProfileDeviceTitle = "الملف النشط على هذا الجهاز",
    activeProfileDeviceDesc = "سيشاهد الطفل فقط الفيديوهات المخصصة للملف المحدد.",
    yearsOld = "سنوات",

    createProfileTitle = "إنشاء ملف شخصي للطفل",
    editProfileTitle = "تعديل الملف الشخصي",
    childNameLabel = "اسم الطفل",
    childAgeLabel = "العمر",
    chooseAvatarEmoji = "اختر رمزاً تعبيرياً:",
    saveBtn = "حفظ",
    cancelBtn = "إلغاء",

    addSingleVideoTitle = "إضافة فيديو يوتيوب",
    youtubeUrlOrIdLabel = "رابط أو معرف فيديو يوتيوب",
    youtubeUrlHint = "مثال: https://www.youtube.com/watch?v=...",
    videoTitleLabel = "عنوان مخصص (اختياري)",
    ageMinLabel = "الحد الأدنى للعمر",
    ageMaxLabel = "الحد الأقصى للعمر",
    targetProfileLabel = "تخصيص للملف",
    addBtn = "إضافة",

    addChannelTitle = "استيراد قناة يوتيوب",
    channelUrlOrIdLabel = "معرف أو رابط القناة",
    channelUrlHint = "مثال: @اسم_القناة أو UC...",
    maxVideosToFetch = "عدد الفيديوهات الأخيرة للاستيراد",
    importBtn = "استيراد القناة",

    settingsTitle = "إعدادات التطبيق",
    sectionGeneral = "الإعدادات العامة",
    languageLabel = "لغة التطبيق",
    languageDesc = "اختر لغة واجهة المستخدم",
    themeLabel = "المظهر والتصميم",
    themeDesc = "فاتح، داكن أو حسب النظام",
    themeSystem = "النظام",
    themeLight = "فاتح",
    themeDark = "داكن",

    sectionSecurity = "الأمان والتحكم الأبوي",
    pinLabel = "رمز PIN السري",
    pinDesc = "رمز من 4 أرقام مطلوب لفتح مساحة الوالدين",
    changePinBtn = "تغيير PIN",
    biometricLabel = "البصمة الحيوية",
    biometricDesc = "بصمة الإصبع / التعرف على الوجه",

    sectionCloud = "المزامنة السحابية والعائلية",
    cloudStatusLabel = "حالة المزامنة السحابية",
    cloudStatusOnline = "متصل ومزامن",
    cloudStatusOffline = "غير متصل",

    sectionAbout = "حول تطبيق PrivaTube",
    aboutDesc = "PrivaTube هو مشغل يوتيوب آمن للأطفال، يتيح للوالدين التحكم الكامل بالمحتوى المسموح رؤيته.",
    appVersionLabel = "الإصدار 2.0.0",

    addContentTitle = "إضافة محتوى يوتيوب",
    editVideoTitle = "تعديل الفيديو",
    tabSingleVideo = "فيديو منفرد",
    tabEntireChannel = "قناة كاملة",
    step1VideoLink = "1. رابط الفيديو",
    pasteUrlLabel = "لصق رابط الفيديو",
    pasteBtn = "لصق",
    thumbnailPreviewLabel = "معاينة الصورة المصغرة:",
    invalidUrlFormat = "تنسيق الرابط غير معروف. تأكد من استخدام رابط يوتيوب صحيح.",
    step2VideoInfo = "2. معلومات الفيديو",
    customTitleLabel = "عنوان مخصص للطفل *",
    customTitleHint = "مثال: مغامرات ميكي ماوس",
    categoryLabel = "التصنيف",
    assignProfileLabel = "تخصيص لملف الطفل",
    parentNoteLabel = "وصف / ملاحظة الوالدين (اختياري)",
    addToLibraryBtn = "إضافة إلى المكتبة",
    saveChangesBtn = "حفظ التغييرات",
    channelInputTitle = "رابط أو اسم القناة",
    channelInputDesc = "أدخل اسم القناة (مثل: cocomelon@) أو رابط القناة الكامل على يوتيوب.",
    channelInputLabel = "اسم القناة أو الرابط",
    channelInputHint = "cocomelon@ أو https://youtube.com/@...",
    analyzingChannelMsg = "جاري تحليل القناة...",
    analyzeChannelBtn = "تحليل قناة يوتيوب",
    channelPrefix = "القناة:",
    videosSelectedCount = "فيديو محدد",
    deselectAll = "إلغاء تحديد الكل",
    selectAll = "تحديد الكل",
    channelCategoryLabel = "تصنيف القناة المستوردة",
    importVideosBtnPrefix = "استيراد",
    importSuccessToast = "فيديو تم استيرادها بنجاح من القناة!",

    pinModalDefaultTitle = "مساحة الوالدين",
    pinModalDefaultSubtitle = "أدخل رمز PIN الخاص بك أو استخدم البصمة الحيوية",
    pinIncorrectError = "رمز PIN غير صحيح. حاول مرة أخرى.",
    biometricButtonLabel = "البصمة / التعرف على الوجه",
    closeBtn = "إغلاق",
    clearBtn = "مسح",

    quickAddTitle = "إضافة محتوى سريعة",
    importChannelBtn = "📺 استيراد قناة",
    searchLibraryPlaceholder = "البحث في المكتبة...",
    noVideosPrompt = "لا توجد فيديوهات. اضغط + إضافة!",
    noSearchResults = "لا توجد نتائج لـ",
    deleteVideoConfirmTitle = "حذف هذا الفيديو؟",
    deleteVideoConfirmText = "هل أنت تأكد من رغبتك في حذف هذا الفيديو من المكتبة؟",
    appAboutDesc = "التحكم الأبوي الخاص لفيديوهات الأطفال مع المزامنة السحابية متعددة الأجهزة."
)

fun getAppStrings(languageCode: String): AppStrings {
    return when (languageCode.lowercase()) {
        "en" -> EnglishStrings
        "ar" -> ArabicStrings
        else -> FrenchStrings
    }
}

val LocalAppStrings = compositionLocalOf { FrenchStrings }
val LocalAppLanguage = compositionLocalOf { AppLanguage.FR }
