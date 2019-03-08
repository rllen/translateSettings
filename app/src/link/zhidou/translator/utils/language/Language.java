package link.zhidou.translator.utils.language;

import java.io.Serializable;

/**
 * 文件名：Language
 * 描  述：语言枚举类
 * 作  者：czm
 * 时  间：2017/9/27
 * 版  权：
 */
public enum Language implements Serializable {
    //TODO 使用的时候务必把下滑线"_"替换成减号"-"
//1
    Chinese_Sichuanese,     //四川话                   中国              四川话         1
    Chinese_DongBei,        //东北话                   中国              东北话
    Chinese_HeNan,          //河南话                   中国              河南话
    Chinese_huNan,          //湖南话                   中国              湖南话
    Chinese_ShanXi,         //陕西话                   中国              陕西话
//2
    zh_CN,                  //普通话                   中国              普通话         2
    zh_TW,                  //台湾话                   中国              台灣話
    zh_HK,                  //粤语                     中国              粵語
    en_US,                  //英语                     美国              English(US)
    en_AU,                  //英语                     澳大利亚           English(AU)
//3
    en_CA,                  //英语                     加拿大             English(CA)    3
    en_GB,                  //英语                     英国               English(UK)
    en_GH,                  //英语                     加纳
    en_IE,                  //英语                     爱尔兰
    en_KE,                  //英语                     肯尼亚
//4
    en_NG,                  //英语                     尼日利亚                           4
    en_PH,                  //英语                     菲律宾
    en_ZA,                  //英语                     南非
    en_TZ,                  //英语                     坦桑尼亚
    en_IN,                  //英语                     印度               English(IN)
//5
    en_NZ,                  //英语                     新西兰             English(NZ)        5
    ar_EG,                  //阿拉伯语                  埃及              اللغة العربية (  مصر) 
    ar_SA,                  //阿拉伯语                  沙特阿拉伯         اللغة العربية (  مصر) 
    ar_IL,                  //阿拉伯语                  以色列
    ar_JO,                  //阿拉伯语                  约旦
//6
    ar_AE,                  //阿拉伯语                  阿拉伯联合酋长国            6
    ar_BH,                  //阿拉伯语                  巴林
    ar_DZ,                  //阿拉伯语                  阿尔及利亚
    ar_IQ,                  //阿拉伯语                  伊拉克
    ar_KW,                  //阿拉伯语                  科威特
//7
    ar_MA,                  //阿拉伯语                  摩洛哥                     7
    ar_TN,                  //阿拉伯语                  突尼斯
    ar_OM,                  //阿拉伯语                  阿曼
    ar_PS,                  //阿拉伯语                  巴勒斯坦国
    ar_QA,                  //阿拉伯语                  卡塔尔
//8
    ar_LB,                  //阿拉伯语                  黎巴嫩                     8
    da_DK,                  //丹麦语                    丹麦              dansk(DK)
    de_DE,                  //德语                      德国              Deutsch(DE)
    ca_ES,                  //加泰罗尼亚文               加泰罗尼亚         Español(ES)
    es_MX,                  //西班牙语                  墨西哥             Español(MX)
//9
    es_AR,                  //西班牙语                  阿根廷                     9
    es_BO,                  //西班牙语                  玻利维亚
    es_CL,                  //西班牙语                  智利
    es_CO,                  //西班牙语                  哥伦比亚
    es_CR,                  //西班牙语                  哥斯达黎加
//10
    es_EC,                  //西班牙语                  厄瓜多尔                    10
    es_SV,                  //西班牙语                  萨尔瓦多
    es_US,                  //西班牙语                  美国
    es_GT,                  //西班牙语                  危地马拉
    es_HN,                  //西班牙语                  洪都拉斯
//11
    es_NI,                  //西班牙语                  尼加拉瓜                    11
    es_PY,                  //西班牙语                  巴拉圭
    es_PE,                  //西班牙语                  秘鲁
    es_PR,                  //西班牙语                  波多黎各
    es_DO,                  //西班牙语                  多明尼加共和国
//12
    es_UY,                  //西班牙语                  乌拉圭                     12
    es_VE,                  //西班牙语                  委内瑞拉
    eu_ES,                  //巴斯克语                  巴斯克语
    fil_PH,                 //菲律宾语                  菲律宾
    gl_ES,                  //加利西亚语                西班牙语
//13
    pt_BR,                  //葡萄牙语                  巴西               Português(BR)      13
    pt_PT,                  //葡萄牙语                  葡萄牙              Português(PT)
    ru_RU,                  //俄语                      俄罗斯              Pусский
    sv_SE,                  //瑞典语                    瑞典                Svenska
    cs_CZ,                  //捷克语                    捷克                Česky
//14
    hu_HU,                  //匈牙利语                  匈牙利               Magyar            14
    ro_RO,                  //罗马尼亚语                 罗马尼亚            românesc
    th_TH,                  //泰语                      泰国                ภาษาไทย
    vi_VN,                  //越南语                    越南                Việt Nam Văn
    el_GR,                  //希腊语                    希腊                Ελληνική γλώσσα
//15
    bg_BG,                  //保加利亚                        на български                  15
    es_PA,                  //西班牙语          巴拿马
    sl_SI,                  //斯洛文尼亚文                       Slovenščina
    es_ES,                  //西班牙(西班牙)                      España
    nb_NO,                  //挪威语                               Norwegian (Bokmål)
//16
    hi_IN,                  //北印度语                              Hindi (India)           16
    sk_SK,                  //斯洛伐克语       斯洛伐克语                 slovenského jazyk
    ko_KR,                  //朝鲜语                    韩国               한국어
    ja_JP,                  //日语                      日本              日本語
    de_AT,                  //德语(奥地利)
//17
    de_CH,                  //德语(瑞士)
    fr_FR,                  //法语                      法国               Français
    fr_CH,                  //法语(瑞士)
    fr_CA,                  //法语                      加拿大             Français(CA)
    he_IL,                  //希伯来语(以色列)
//18
    id_ID,                  //印尼语(印尼)
    tr_TR,                  //土耳其语(土耳其)
    ua_UK,                  //乌克兰语(乌克兰)
    bn_BD,                  //孟加拉语(孟加拉国)
    bn_IN,                  //孟加拉语(印度)
//19
    ms_MY,                  //马来语
    fa_IR,                  //波斯语
    sr_RS,                  //塞尔维亚语
    ur_PK,                  //乌尔都语
    ur_IN,                  //乌尔都语                  印度
//20
    af_ZA,                  //南非荷兰语(南非)
    am_ET,                  //阿姆哈拉语
    hy_AM,                  //亚美尼亚语
    az_AZ,                  //阿塞拜疆语
    ka_GE,                  //格鲁吉亚语                格鲁吉亚
//21
    gu_IN,                  //古吉拉特语                印度
    hr_HR,                  //克罗地亚语文               克罗地亚
    zu_ZA,                  //南非祖鲁语(南非)
    is_IS,                  //冰岛语(冰岛)
    it_IT,                  //意大利语                   意大利            Italiano(IT)
//22
    jv_ID,                  //爪哇语(印度尼西亚)
    kn_IN,                  //卡纳达语(印度)
    km_KH,                  //高棉语(柬埔寨)
    l1_LA,                  //老挝语(老挝)
    lv_LV,                  //拉脱维亚语(拉脱维亚)
//23
    lt_LT,                  //立陶宛语(立陶宛)
    ml_IN,                  //马拉雅拉姆语(印度)
    mr_IN,                  //马拉地语(印度)
    nl_NL,                  //荷兰语                    荷兰               Nederlands(NL)
    ne_NP,                  //尼泊尔
//24
    pl_PL,                  //波兰语                    波兰               Polski
    si_LK,                  //僧伽罗语(斯里兰卡)
    su_ID,                  //巽语(印度尼西亚)
    sw_TZ,                  //斯瓦希里语(坦桑尼亚)
    sw_KE,                  //斯瓦希里语(肯尼亚)
//25
    fi_FI,                  //芬兰语                    芬兰               Suomen
    ta_IN,                  //泰米尔语(印度)
    ta_SG,                  //泰米尔语(新加坡)
    ta_LK,                  //泰米尔语(斯里兰卡)
    ta_MY,                  //泰米尔语(马来西亚)
//26
    te_IN,                  //泰卢固语(印度)
    my_MM,                  // 缅甸语 (缅甸)
    lo_LA,                  // 老挝（老挝）
    none                    //未知语言
    ;

    public static Language from(String code) {
        return Language.valueOf(code);
    }

    public static String toTag(Language language) {
        return language.toString().replace("_", "-");
    }

    public static String toTag(String code) {
        return code.replace("_", "-");
    }

    public static Language fromTag(String tag) {
        final String code = tag.replaceAll("-", "_");
        return Language.from(code);
    }

}
