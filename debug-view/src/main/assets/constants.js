/**
 * Application Constants
 * Contains static data for Navigation and Social links
 */

const APP_CONSTANTS = {
    SOCIAL_LINKS: [
        { name: 'Gmail', url: 'mailto:gmddev074@gmail.com', icon: 'gmail', color: 'EA4335' },
        { name: 'GitHub', url: 'https://github.com/gmddev074', icon: 'github', color: '181717' },
        { name: 'WhatsApp', url: 'https://wa.me/+923082456659', icon: 'whatsapp', color: '25D366' },
        { name: 'Telegram', url: 'https://t.me/@Itxz_Sallu', icon: 'telegram', color: '26A5E4' },
        { name: 'IMO', url: 'https://profile.imo.im/profileshare/shr.AAAAAAAAAAAAAAAAAAAAAK1Xu5Axl0LvwrC2PtO0_QnSvxQIUrGVloBFJJOmL-qD', icon: 'message', color: '00AFF0' }, /* Using generic message icon as IMO might not be in simple icons CDN */
        { name: 'LinkedIn', url: 'https://www.linkedin.com/in/muhammad-salman-5672a0203', icon: 'linkedin', color: '0A66C2' }
    ],

    OTHER_PROJECTS: [
        { name: 'Custom Toggle', url: 'https://github.com/GmdDev074/CustomToggleDemo' },
        { name: 'Custom Calendar', url: 'https://github.com/GmdDev074/CustomCalendar' },
        { name: 'Custom Shimmer', url: 'https://github.com/GmdDev074/CustomShimmer' }
    ],

    // Database Types Enums
    DB_TYPES: {
        SHARED_PREFS: 'SHARED_PREFS',
        ROOM_SQLITE: 'ROOM_SQLITE',
        PAPER_DB: 'PAPER_DB'
    }
};
