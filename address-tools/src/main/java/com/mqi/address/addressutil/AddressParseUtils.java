package com.mqi.address.addressutil;

import com.mqi.address.jsonutil.JacksonUtil;
import com.mqi.address.addressutil.*;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.stereotype.Component;
import org.springframework.util.ResourceUtils;
import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 地址清洗
 */
@Component
public class AddressParseUtils {

    private static final Logger log = LoggerFactory.getLogger(AddressParseUtils.class);
    private static ThreadLocal<String> threadLocal = new ThreadLocal<>();

    public void befor() throws IOException {
        log.info("*********thread={}开始前threadLocal={}",Thread.currentThread().getName(),JacksonUtil.BeanToJson(threadLocal));
        threadLocal.set(Thread.currentThread().getName());
    }

    public void after() throws IOException{
        log.info("*********thread={}开始后threadLocal={}",Thread.currentThread().getName(),JacksonUtil.BeanToJson(threadLocal));
        threadLocal.remove();
    }

    /**
     * 省市县json数据文本集合。
     */
    private static  final Map<String,String> ADDRESS_JSON_MAP = new HashMap<>();

    @PostConstruct
    public void init() throws IOException {
        addressJsonListInit();

    }
//    static {
//    try {
//        addressJsonListInit();
//    } catch (IOException ioException) {
//        throw new RuntimeException("初始化数据失败");
//    }
//}

    /**
     * 初始化，或者重新复制地址文本集合
     */
    private static void addressJsonListInit() throws IOException {
        File provinceFile = ResourceUtils.getFile("classpath:provinces.json");
        String provinceStr = FileUtils.readFileToString(provinceFile);
        File cityFile = ResourceUtils.getFile("classpath:cities.json");
        String cityStr = FileUtils.readFileToString(cityFile);
        File areaFile = ResourceUtils.getFile("classpath:areas.json");
        String areaStr = FileUtils.readFileToString(areaFile);
        if (StringUtils.isBlank(provinceStr) && StringUtils.isBlank(cityStr) && StringUtils.isBlank(areaStr)){
            throw new RuntimeException("地区初始化错误");
        }


        ADDRESS_JSON_MAP.put("province", StringUtils.isNoneBlank(provinceStr)? provinceStr : "");
        ADDRESS_JSON_MAP.put("city",StringUtils.isNoneBlank(cityStr)? cityStr : "");
        ADDRESS_JSON_MAP.put("area",StringUtils.isNoneBlank(areaStr)? areaStr : "");
    }

    /**
     * 地址是否更新标记键
     */
    private static  final  String  ADDRESS_INFO_UPDATE_SIGN_KEY = "address:info:update:sign:key:";
    /**
     * 名字字符最大长度
     */
    private static  final  Integer  NAME_MAX_LENGTH = 4;

    /**
     * 直辖市清洗字段
     */
    private static  final  List<List<String>>  DIRECTLY_CITY = Arrays.asList(
            Arrays.asList("北京市","北京"),Arrays.asList("天津市","天津"),
            Arrays.asList("上海市","上海"),Arrays.asList("重庆市","重庆"));

    /**
     * 自治区清洗字段
     */
    private static  final  List<List<String>>  MUNICIPALITY = Arrays.asList(
            Arrays.asList("广西壮族自治区","广西"),Arrays.asList("西藏自治区","西藏"),
            Arrays.asList("宁夏回族自治区","宁夏"),Arrays.asList("新疆维吾尔自治区","新疆"),
            Arrays.asList("内蒙古自治区","内蒙古"));



    /**
     * 去除换行符正则
     */
    private static  final  String  ENTER_RULE = "\r\n|\n|\t";
    /**
     * 地址中的特殊字符
     */
    private static  final  String  ERROR_CHAR_RULE = "[`~!@#$%^&*()+=|{}':;',\\[\\].<>/?~！@#￥%……&*（）——+|{}【】'；：”“’。，、？]";
    /**
     * 2个或2个以上空格正则
     */
    private static  final  String  SPACE_RULE = "\\s{2,}";
    /**
     * 地址中电话号码正则
     */
    private static  final  String  ADDRESS_PHONE_RULE = "(\\d{7,12})|(\\d{3,4}-\\d{6,8})|(86-[1][0-9]{10})|(86[1][0-9]{10})|([1][0-9]{10})";
    /**
     * 地址中邮政编码正则
     */
    private static  final  String  ADDRESS_POSTAL_CODE_RULE = "\\d{6}";
    /**
     * 自定义关键字
     */
    private static  final List<String> SEARCH_LIST = Arrays.asList("详细地址","收货地址","收件地址", "地址", "所在地区", "地区",
            "姓名", "收货人", "收件人", "联系人", "收", "邮编", "联系电话", "电话", "联系人手机号码", "手机号码", "手机号", "自治区直辖县级行政区划", "省直辖县级行政区划","电話","電話","聯系人");

    /**
     * 姓名包含的称谓即为名字
     */
    private static final List<String> NAME_CALL = Arrays.asList("先生", "小姐","女士","老师", "同志", "哥哥", "姐姐", "妹妹", "弟弟", "妈妈", "爸爸", "爷爷", "奶奶", "姑姑", "舅舅");

    /**
     * 查找地址中含有的姓名过滤字段信息
     */
    private static final List<String> NAME_CALL_FILTER = Arrays.asList("街道", "乡镇");

    /**
     * 百家姓
     */
    private static final String FAMILY_NAME_STR = "[\"赵\",\"钱\",\"孙\",\"李\",\"周\",\"吴\",\"郑\",\"王\",\"冯\",\"陈\",\"楮\",\"卫\",\"蒋\",\"沈\",\"韩\",\"杨\",\"朱\",\"秦\",\"尤\",\"许\",\"何\",\"吕\",\"施\",\"张\",\"孔\",\"曹\",\"严\",\"华\",\"金\",\"魏\",\"陶\",\"姜\",\"戚\",\"谢\",\"邹\",\"喻\",\"柏\",\"水\",\"窦\",\"章\",\"云\",\"苏\",\"潘\",\"葛\",\"奚\",\"范\",\"彭\",\"郎\",\"鲁\",\"韦\",\"昌\",\"马\",\"苗\",\"凤\",\"花\",\"方\",\"俞\",\"任\",\"袁\",\"柳\",\"酆\",\"鲍\",\"史\",\"唐\",\"费\",\"廉\",\"岑\",\"薛\",\"雷\",\"贺\",\"倪\",\"汤\",\"滕\",\"殷\",\"罗\",\"毕\",\"郝\",\"邬\",\"安\",\"常\",\"乐\",\"于\",\"时\",\"傅\",\"皮\",\"卞\",\"齐\",\"康\",\"伍\",\"余\",\"元\",\"卜\",\"顾\",\"孟\",\"平\",\"黄\",\"和\",\"穆\",\"萧\",\"尹\",\"姚\",\"邵\",\"湛\",\"汪\",\"祁\",\"毛\",\"禹\",\"狄\",\"米\",\"贝\",\"明\",\"臧\",\"计\",\"伏\",\"成\",\"戴\",\"谈\",\"宋\",\"茅\",\"庞\",\"熊\",\"纪\",\"舒\",\"屈\",\"项\",\"祝\",\"董\",\"梁\",\"杜\",\"阮\",\"蓝\",\"闽\",\"席\",\"季\",\"麻\",\"强\",\"贾\",\"路\",\"娄\",\"危\",\"江\",\"童\",\"颜\",\"郭\",\"梅\",\"盛\",\"林\",\"刁\",\"锺\",\"徐\",\"丘\",\"骆\",\"高\",\"夏\",\"蔡\",\"田\",\"樊\",\"胡\",\"凌\",\"霍\",\"虞\",\"万\",\"支\",\"柯\",\"昝\",\"管\",\"卢\",\"莫\",\"经\",\"房\",\"裘\",\"缪\",\"干\",\"解\",\"应\",\"宗\",\"丁\",\"宣\",\"贲\",\"邓\",\"郁\",\"单\",\"杭\",\"洪\",\"包\",\"诸\",\"左\",\"石\",\"崔\",\"吉\",\"钮\",\"龚\",\"程\",\"嵇\",\"邢\",\"滑\",\"裴\",\"陆\",\"荣\",\"翁\",\"荀\",\"羊\",\"於\",\"惠\",\"甄\",\"麹\",\"家\",\"封\",\"芮\",\"羿\",\"储\",\"靳\",\"汲\",\"邴\",\"糜\",\"松\",\"井\",\"段\",\"富\",\"巫\",\"乌\",\"焦\",\"巴\",\"弓\",\"牧\",\"隗\",\"山\",\"谷\",\"车\",\"侯\",\"宓\",\"蓬\",\"全\",\"郗\",\"班\",\"仰\",\"秋\",\"仲\",\"伊\",\"宫\",\"宁\",\"仇\",\"栾\",\"暴\",\"甘\",\"斜\",\"厉\",\"戎\",\"祖\",\"武\",\"符\",\"刘\",\"景\",\"詹\",\"束\",\"龙\",\"叶\",\"幸\",\"司\",\"韶\",\"郜\",\"黎\",\"蓟\",\"薄\",\"印\",\"宿\",\"白\",\"怀\",\"蒲\",\"邰\",\"从\",\"鄂\",\"索\",\"咸\",\"籍\",\"赖\",\"卓\",\"蔺\",\"屠\",\"蒙\",\"池\",\"乔\",\"阴\",\"郁\",\"胥\",\"能\",\"苍\",\"双\",\"闻\",\"莘\",\"党\",\"翟\",\"谭\",\"贡\",\"劳\",\"逄\",\"姬\",\"申\",\"扶\",\"堵\",\"冉\",\"宰\",\"郦\",\"雍\",\"郤\",\"璩\",\"桑\",\"桂\",\"濮\",\"牛\",\"寿\",\"通\",\"边\",\"扈\",\"燕\",\"冀\",\"郏\",\"浦\",\"尚\",\"农\",\"温\",\"别\",\"庄\",\"晏\",\"柴\",\"瞿\",\"阎\",\"充\",\"慕\",\"连\",\"茹\",\"习\",\"宦\",\"艾\",\"鱼\",\"容\",\"向\",\"古\",\"易\",\"慎\",\"戈\",\"廖\",\"庾\",\"终\",\"暨\",\"居\",\"衡\",\"步\",\"都\",\"耿\",\"满\",\"弘\",\"匡\",\"国\",\"文\",\"寇\",\"广\",\"禄\",\"阙\",\"东\",\"欧\",\"殳\",\"沃\",\"利\",\"蔚\",\"越\",\"夔\",\"隆\",\"师\",\"巩\",\"厍\",\"聂\",\"晁\",\"勾\",\"敖\",\"融\",\"冷\",\"訾\",\"辛\",\"阚\",\"那\",\"简\",\"饶\",\"空\",\"曾\",\"毋\",\"沙\",\"乜\",\"养\",\"鞠\",\"须\",\"丰\",\"巢\",\"关\",\"蒯\",\"相\",\"查\",\"后\",\"荆\",\"红\",\"游\",\"竺\",\"权\",\"逑\",\"盖\",\"益\",\"桓\",\"公\",\"万俟\",\"司马\",\"上官\",\"欧阳\",\"夏侯\",\"诸葛\",\"闻人\",\"东方\",\"赫连\",\"皇甫\",\"尉迟\",\"公羊\",\"澹台\",\"公冶\",\"宗政\",\"濮阳\",\"淳于\",\"单于\",\"太叔\",\"申屠\",\"公孙\",\"仲孙\",\"轩辕\",\"令狐\",\"锺离\",\"宇文\",\"长孙\",\"慕容\",\"鲜于\",\"闾丘\",\"司徒\",\"司空\",\"丌官\",\"司寇\",\"仉\",\"督\",\"子车\",\"颛孙\",\"端木\",\"巫马\",\"公西\",\"漆雕\",\"乐正\",\"壤驷\",\"公良\",\"拓拔\",\"夹谷\",\"宰父\",\"谷梁\",\"晋\",\"楚\",\"阎\",\"法\",\"汝\",\"鄢\",\"涂\",\"钦\",\"段干\",\"百里\",\"东郭\",\"南门\",\"呼延\",\"归\",\"海\",\"羊舌\",\"微生\",\"岳\",\"帅\",\"缑\",\"亢\",\"况\",\"后\",\"有\",\"琴\",\"梁丘\",\"左丘\",\"东门\",\"西门\",\"商\",\"牟\",\"佘\",\"佴\",\"伯\",\"赏\",\"南宫\",\"墨\",\"哈\",\"谯\",\"笪\",\"年\",\"爱\",\"阳\",\"佟\",\"第五\",\"言\",\"福\"]";



    /**
     * 地址解析省市县姓名电话邮编等
     * @param address
     * @return
     */
    public static ParseResult parseAddress(String address) throws IOException {
        ParseResult parseResult = new ParseResult();

        if (StringUtils.isBlank(address)){
            log.info("AddressParseUtils-parseAddress-地址字符串不存在address={}",address);
            throw new RuntimeException("地址解析参数不能为空");
        }
        //清理地址数据
        address = cleanAddress(address);

        //识别手机号
        address = filterPhone(address,parseResult);

        //获取邮编号码
        address = filterPostalCode(address,parseResult);


        //地址分割
        List<String> addressList = addressSplit(address);

        //解析省市县
        addressList = regexpParseAddressList(addressList,parseResult);

        //检查清理省市县地址去重
        addressList = distinctCheck(addressList,parseResult);

        //拷贝一份去重排序去识别姓名
        List<String> addressListCopy = copyDistinctSort(addressList);

        //识别地址中的姓名
        filterName(addressListCopy,parseResult);

        //包装地址详细地址
        packageDetail(addressList,parseResult);

        return parseResult;
    }




    /**
     * 地址清洗
     * @param address
     */
    private static String cleanAddress(String address) {
        //去除换行等数据
        address = address.replaceAll(ENTER_RULE," ");
        log.info("AddressParseUtils-cleanAddress-去除换行后的地址address={}",address);

        //去除地址中的关键字
        String searchListRegExp = StringUtils.join(SEARCH_LIST,"|");
        address = address.replaceAll(searchListRegExp," ");
        log.info("AddressParseUtils-cleanAddress-去除地址中的关键字后的地址address={}",address);

        //除去特殊字符
        address = address.replaceAll(ERROR_CHAR_RULE," ");
        log.info("AddressParseUtils-cleanAddress-去除特殊字符后的地址address={}",address);

        //去除地址中2个或2个以上的空格
        address = address.replaceAll(SPACE_RULE," ");
        log.info("AddressParseUtils-cleanAddress-去除地址中2个或2个以上的空格后的地址address={}",address);

        String regexp = "";
        //直辖市处理，出现两次的直辖市进行去除
        for (List<String> itemList : DIRECTLY_CITY) {
            if (CollectionUtils.isEmpty(itemList) || itemList.size() < 2){
                continue;
            }
            if (StringUtils.isNoneBlank(regexp)){
                regexp = "";
            }
            String replaceStr = itemList.get(0);
            regexp = StringUtils.join(itemList,"|");
            address = address.replaceAll(regexp,replaceStr);
        }

        //自治区处理，出现两次的自治区进行去除
        for (List<String> itemList : MUNICIPALITY) {
            if (CollectionUtils.isEmpty(itemList) || itemList.size() < 2){
                continue;
            }
            if (StringUtils.isNoneBlank(regexp)){
                regexp = "";
            }
            regexp = StringUtils.join(itemList,"|");
            String replaceStr = itemList.get(1);
            address = address.replaceAll(regexp,replaceStr);
        }
        log.info("AddressParseUtils-cleanAddress-去除清洗直辖市自治区后的地址address={}",address);
        return address;
    }


    /**
     * 识别地址中的手机号
     * @param address
     * @param parseResult
     */
    private static String filterPhone(String address, ParseResult parseResult) throws IOException {
        if (Objects.isNull(parseResult) || StringUtils.isBlank(address)){
            throw new RuntimeException("地址解析参数不能为空");
        }
        parseResult.setPhone("");
        address  = address.replaceAll("(\\d{3})-(\\d{4})-(\\d{4})","$1$2$3");
        address  = address.replaceAll("(\\d{3}) (\\d{4}) (\\d{4})","$1$2$3");
        address  = address.replaceAll("\\d{4} \\d{4} \\d{4}","$1$2$3");
        address  = address.replaceAll("(\\d{4})","$1");

        Pattern addressPhoneRulePattern = Pattern.compile(ADDRESS_PHONE_RULE);
        Matcher addressPhoneRuleMatcher = addressPhoneRulePattern.matcher(address);
        if (addressPhoneRuleMatcher.find()){
            String phone = addressPhoneRuleMatcher.group();
            log.info("AddressParseUtils-filterPhone-address={}解析的电话号码是phone={}",address,phone);
            parseResult.setPhone(phone);
            address = address.replaceAll(phone," ");
        }
        log.info("AddressParseUtils-filterPhone-解析出电话后的地址address={},parseResult={}",address,JacksonUtil.BeanToJson(parseResult));
        return address;
    }


    /**
     * 获取地址中的邮政编码
     * @param address
     * @param parseResult
     * @return
     */
    private static String filterPostalCode(String address, ParseResult parseResult) throws IOException {
        if (Objects.isNull(parseResult) || StringUtils.isBlank(address)){
            throw new RuntimeException("地址解析参数不能为空");
        }
        parseResult.setPostalCode("");
        Pattern addressPostalCodeRulePattern = Pattern.compile(ADDRESS_POSTAL_CODE_RULE);
        Matcher addressPostalCodeRuleMatcher = addressPostalCodeRulePattern.matcher(address);
        if (addressPostalCodeRuleMatcher.find()){
            String postalCode = addressPostalCodeRuleMatcher.group();
            log.info("AddressParseUtils-filterPostalCode-address={}解析的邮编号码是postalCode={}",address,postalCode);
            parseResult.setPostalCode(postalCode);
            address = address.replaceAll(postalCode," ");
        }
        log.info("AddressParseUtils-filterPostalCode-解析出邮编号码后的地址address={},parseResult={}",address,JacksonUtil.BeanToJson(parseResult));
        return address;
    }

    /**
     * 地址分割得到集合
     * @param address
     * @return
     */
    private static List<String> addressSplit(String address) throws IOException {
        if (StringUtils.isBlank(address)){
            throw new RuntimeException("地址解析参数不能为空");
        }
        String[] split = address.split(" ");
        if (ArrayUtils.isEmpty(split)){
            throw new RuntimeException("地址解析参数异常");
        }
        List<String> addressList = Arrays.asList(split);
        if (CollectionUtils.isNotEmpty(addressList)){
            addressList = new ArrayList<>(addressList);
            addressList.removeAll(Collections.singleton(""));
            addressList.removeAll(Collections.singleton(null));
        }
        log.info("AddressParseUtils-addressSplit-地址分割得到集合addressList={}",JacksonUtil.BeanToJson(addressList));
        return addressList;
    }


    /**
     * 解析省市县去重排序
     * @param addressList
     * @param parseResult
     */
    private static List<String> regexpParseAddressList(List<String> addressList, ParseResult parseResult) throws IOException {
        if (Objects.isNull(parseResult) || CollectionUtils.isEmpty(addressList)){
            throw new RuntimeException("地址解析参数不能为空");
        }
        //解析出省市县后的地址
        List<String> addressResultList = new ArrayList<>();



        //解析省市县
        for (String addressItem : addressList) {
            if (Objects.isNull(parseResult.getProvinceId())
                    || Objects.isNull(parseResult.getCityId())
                    || Objects.isNull(parseResult.getAreaId())){
                //正则解析地址
                addressItem = parseRegionWithRegexp(addressItem,parseResult);
                addressResultList.add(addressItem);
            }else {
                addressResultList.add(addressItem);
            }
        }
        log.info("AddressParseUtils-regexpParseAddressList-result-addressResultList={}",JacksonUtil.BeanToJson(addressResultList));
        return addressResultList;
    }


    public static void main(String[] args) {
        String source = "{\"pId\":\"21\",\"pName\":\"辽宁省\"},{\"pId\":\"22\",\"pName\":\"吉林省\"},{\"pId\":\"23\",\"pName\":\"黑龙江省\"}";
        String provinceNameRegExp = "\\{\"pId\":\"[0-9]{1,}\",\"pName\":\""+"黑龙"+"[\u4e00-\u9fa5]*\"}";
        Pattern provinceNameRegExpPattern = Pattern.compile(provinceNameRegExp);
        Matcher provinceNameRegExpMatcher = provinceNameRegExpPattern.matcher(source);
        System.out.println(provinceNameRegExpMatcher.find());
        System.out.println(provinceNameRegExpMatcher.group());
    }

    /**
     * 正则解析地址
     * @param addressItem
     * @param parseResult
     */
    private static String parseRegionWithRegexp(String addressItem, ParseResult parseResult) throws IOException {
        if (Objects.isNull(parseResult) || StringUtils.isBlank(addressItem)){
            throw new RuntimeException("地址解析参数不能为空");
        }
        log.info("AddressParseUtils-parseRegionWithRegexp-param-addressItem={};parseResult={}",addressItem,JacksonUtil.BeanToJson(parseResult));

        addressItem = addressItem.trim();
        String matchStr = "";

        String provincesJsonListStr = ADDRESS_JSON_MAP.get("province");
        String cityJsonListStr = ADDRESS_JSON_MAP.get("city");
        String areaJsonListStr = ADDRESS_JSON_MAP.get("area");

        ProvinceJson provinceJson = null;
        CityJson cityJson = null;
        AreaJson areaJson = null;
        //不存在省id
        if (Objects.isNull(provinceJson) && StringUtils.isNoneBlank(addressItem)){

            for (int i = 1; i < addressItem.length(); i++){
                String addressItemSubstring = addressItem.substring(0, i + 1);
                String provinceNameRegExp = "\\{\"pId\":\"[0-9]{1,}\",\"pName\":\""+addressItemSubstring+"[\u4e00-\u9fa5]*\"\\}";
                Pattern provinceNameRegExpPattern = Pattern.compile(provinceNameRegExp);
                Matcher provinceNameRegExpMatcher = provinceNameRegExpPattern.matcher(provincesJsonListStr);

                List<String> provinceMatcherResult = matcherResult(provinceNameRegExpMatcher);

                if (CollectionUtils.isNotEmpty(provinceMatcherResult)){
                    String provinceJsonStr = provinceMatcherResult.get(0);
                    ProvinceJson provinceJsonTemp = JacksonUtil.readValue(provinceJsonStr,ProvinceJson.class);
                    if (provinceMatcherResult.size() == 1){
                        provinceJson = null;
                        matchStr = addressItemSubstring;
                        provinceJson = provinceJsonTemp;
                    }
                }else {
                    log.info("AddressParseUtils-parseRegionWithRegexp-param-省级-addressItem={}-解析addressItemSubstring={}不存在，",addressItem,addressItemSubstring);
                    break;
                }
            }

            if (Objects.nonNull(provinceJson)){
                addressItem = addressItem.replaceAll(matchStr,"");
            }
        }

        //不存在市id
        if (Objects.isNull(cityJson) && StringUtils.isNoneBlank(addressItem)) {
            for (int i = 1; i < addressItem.length(); i++) {
                String addressItemSubstring = addressItem.substring(0, i + 1);
                String cityNameRegExp = "\\{\"cId\":\"[0-9]{1,}\",\"cName\":\"" + addressItemSubstring + "[\\\u4e00-\\\u9fa5]*\",\"pId\":\"" + (Objects.nonNull(provinceJson) ? provinceJson.getpId() : "[0-9]{1,}") + "\"}";
                Pattern cityNameRegExpPattern = Pattern.compile(cityNameRegExp);
                Matcher cityNameRegExpMatcher = cityNameRegExpPattern.matcher(cityJsonListStr);

                List<String> cityMatcherResult = matcherResult(cityNameRegExpMatcher);

                if (CollectionUtils.isNotEmpty(cityMatcherResult)){
                    String cityJsonStr = cityMatcherResult.get(0);
                    CityJson cityJsonTemp = JacksonUtil.readValue(cityJsonStr, CityJson.class);
                    if (cityMatcherResult.size() == 1){
                        cityJson = null;
                        matchStr = addressItemSubstring;
                        cityJson = cityJsonTemp;
                    }
                }else {
                    log.info("AddressParseUtils-parseRegionWithRegexp-param-市级-addressItem={}-解析addressItemSubstring={}不存在，",addressItem,addressItemSubstring);
                    break;
                }
            }

            if (Objects.nonNull(cityJson)) {
                addressItem = addressItem.replaceAll(matchStr, "");

                //如果不存在省对象根据市向上查
                if (Objects.isNull(provinceJson)){
                    String provinceNameRegExp = "\\{\"pId\":\""+cityJson.getpId()+"\",\"pName\":\"[\\\u4e00-\\\u9fa5]*\"}";
                    Pattern provinceNameRegExpPattern = Pattern.compile(provinceNameRegExp);
                    Matcher provinceNameRegExpMatcher = provinceNameRegExpPattern.matcher(provincesJsonListStr);
                    if (provinceNameRegExpMatcher.find()){
                        String provinceJsonStr = provinceNameRegExpMatcher.group();
                        provinceJson = JacksonUtil.readValue(provinceJsonStr, ProvinceJson.class);
                    }else {
                        log.info("AddressParseUtils-parseRegionWithRegexp-param-市级-addressItem={}-向上解析省级pId={}不存在，",addressItem,cityJson.getpId());
                    }
                }
            }

        }


        //不存在县id
        if (Objects.isNull(areaJson) && StringUtils.isNoneBlank(addressItem)){

            for (int i = 1; i < addressItem.length(); i++) {
                String addressItemSubstring = addressItem.substring(0, i + 1);
                String areaNameRegExp = "\\{\"aId\":\"[0-9]{1,}\",\"aName\":\""+addressItemSubstring+"[\\\u4e00-\\\u9fa5]*\",\"cId\":\""+ (Objects.nonNull(cityJson)? cityJson.getcId() : "[0-9]{1,}" )+"\",\"pId\":\""+ (Objects.nonNull(provinceJson)?  provinceJson.getpId() :"[0-9]{1,}" )+"\"}";
                Pattern areaNameRegExpPattern = Pattern.compile(areaNameRegExp);
                Matcher areaNameRegExpMatcher = areaNameRegExpPattern.matcher(areaJsonListStr);

                List<String> areaMatcherResult = matcherResult(areaNameRegExpMatcher);

                if (CollectionUtils.isNotEmpty(areaMatcherResult)){
                    String areaJsonStr = areaMatcherResult.get(0);
                    AreaJson areaJsonTemp = JacksonUtil.readValue(areaJsonStr, AreaJson.class);
                    if (areaMatcherResult.size() == 1){
                        areaJson = null;
                        matchStr = addressItemSubstring;
                        areaJson = areaJsonTemp;
                    }
                }else {
                    log.info("AddressParseUtils-parseRegionWithRegexp-param-县级-addressItem={}-解析addressItemSubstring={}不存在，",addressItem,addressItemSubstring);
                    break;
                }
            }

            if (Objects.nonNull(areaJson)) {
                addressItem = addressItem.replaceAll(matchStr, "");

                //如果不存在省对象根据县向上查
                if (Objects.isNull(provinceJson)){
                    String provinceNameRegExp = "\\{\"pId\":\""+areaJson.getpId()+"\",\"pName\":\"[\\\u4e00-\\\u9fa5]*\"}";
                    Pattern provinceNameRegExpPattern = Pattern.compile(provinceNameRegExp);
                    Matcher provinceNameRegExpMatcher = provinceNameRegExpPattern.matcher(provincesJsonListStr);
                    if (provinceNameRegExpMatcher.find()){
                        String provincesJsonStr = provinceNameRegExpMatcher.group();
                        provinceJson = JacksonUtil.readValue(provincesJsonStr, ProvinceJson.class);
                    }else {
                        log.info("AddressParseUtils-parseRegionWithRegexp-param-县级-addressItem={}-向上解析省级pId={}不存在，",addressItem,areaJson.getpId());
                    }
                }

                //如果不存在市对象根据县向上查
                if (Objects.isNull(cityJson)){
                    String cityNameRegExp = "\\{\"cId\":\""+areaJson.getcId()+"\",\"cName\":\"[\\\u4e00-\\\u9fa5]*\",\"pId\":\"[0-9]{0,}\"}";
                    Pattern cityNameRegExpPattern = Pattern.compile(cityNameRegExp);
                    Matcher cityNameRegExpMatcher = cityNameRegExpPattern.matcher(cityJsonListStr);
                    if (cityNameRegExpMatcher.find()){
                        String cityJsonStr = cityNameRegExpMatcher.group();
                        cityJson = JacksonUtil.readValue(cityJsonStr, CityJson.class);
                    }else {
                        log.info("AddressParseUtils-parseRegionWithRegexp-param-县级-addressItem={}-向上解析市级cId={}不存在，",addressItem,areaJson.getcId());
                    }
                }

            }

        }
        if (Objects.nonNull(provinceJson)){
                parseResult.setProvinceId(Long.valueOf(provinceJson.getpId()));
                parseResult.setProvince(provinceJson.getpName());
        }
        if (Objects.nonNull(cityJson)){
                parseResult.setCityId(Long.valueOf(cityJson.getcId()));
                parseResult.setCity(cityJson.getcName());
        }
        if (Objects.nonNull(areaJson)){
                parseResult.setAreaId(Long.valueOf(areaJson.getaId()));
                parseResult.setArea(areaJson.getaName());
        }

        log.info("AddressParseUtils-parseRegionWithRegexp-result-addressItem={};parseResult={}",addressItem,JacksonUtil.BeanToJson(parseResult));
        return addressItem;
    }

    /**
     * 检查清理省市县地址去重
     * @param addressList
     * @param parseResult
     */
    private static List<String> distinctCheck(List<String> addressList, ParseResult parseResult) {
        List<String> addressListCopyTemp = new ArrayList<>();
        String province = StringUtils.isNoneBlank(parseResult.getProvince()) ? parseResult.getProvince() : "";
        String city = StringUtils.isNoneBlank(parseResult.getCity()) ? parseResult.getCity() : "";
        String area = StringUtils.isNoneBlank(parseResult.getArea()) ? parseResult.getArea() : "";
        String regExp = StringUtils.join(Arrays.asList(province, city, area), "|");
        //去除省市县
        for (String address : addressList) {
            address = address.replaceAll(regExp,"");
            addressListCopyTemp.add(address);
        }
        addressList = addressListCopyTemp;

        //去空字符串
        addressList.removeAll(Collections.singleton(""));
        addressList.removeAll(Collections.singleton(null));
        //去重
        addressList =addressList.stream().distinct().collect(Collectors.toList());

        return addressList;
    }

    /**
     * 复制去重拷贝
     * @param addressList
     * @return
     */
    private static List<String> copyDistinctSort(List<String> addressList) throws IOException {
        List<String> addressListCopy = new ArrayList<>(addressList);
        //去重 排序
        if (CollectionUtils.isNotEmpty(addressListCopy)){
            //去重
            addressListCopy =addressListCopy.stream().distinct().collect(Collectors.toList());
            log.info("AddressParseUtils-regexpParseAddressList-解析省市县后去重后的集合addressListCopy={}",JacksonUtil.BeanToJson(addressListCopy));
            /**
             * 去空字符串
             */
            addressListCopy.removeAll(Collections.singleton(""));
            addressListCopy.removeAll(Collections.singleton(null));
            //排序
            addressListCopy.sort(Comparator.comparing(String::length));
            log.info("AddressParseUtils-regexpParseAddressList-解析省市县去重字符串长度排序后的集合addressListCopy={}",JacksonUtil.BeanToJson(addressListCopy));
        }
        return addressListCopy;
    }

    /**
     * 识别地址中的姓名
     * @param addressListCopy
     * @param parseResult
     * @return
     */
    private static void filterName(List<String> addressListCopy, ParseResult parseResult) throws IOException {
        if (Objects.isNull(parseResult) || CollectionUtils.isEmpty(addressListCopy)){
            throw new RuntimeException("地址解析参数不能为空");
        }
        log.info("AddressParseUtils-filterName-param-parseResult={};addressListCopy={}",JacksonUtil.BeanToJson(parseResult),JacksonUtil.BeanToJson(addressListCopy));
        Integer index = -1;
        for (int i = 0; i < addressListCopy.size(); i++) {
            String address = addressListCopy.get(i);
            index = checkStrIndexOfName(address,i);
            if (index > -1){
                break;
            }
        }
        String name = "";
        if (index > -1 ){
            name = addressListCopy.get(index);
        }else {
            String firstString = addressListCopy.get(0);
            String regExp = "^[\u4e00-\u9fa5]*";
            Pattern compile = Pattern.compile(regExp);
            Matcher matcher = compile.matcher(firstString);
            if (firstString.length() <= NAME_MAX_LENGTH && matcher.find()){
                name = firstString;
            }
        }

        if (StringUtils.isNoneBlank(name)){
            parseResult.setName(name);
        }
        log.info("AddressParseUtils-filterName-result-index={};name={};addressListCopy={};ParseResult={}",index,name,JacksonUtil.BeanToJson(addressListCopy),JacksonUtil.BeanToJson(parseResult));
    }

    /**
     * 判断字符是否是名字
     * @param address
     * @param i
     * @return
     */
    private static Integer checkStrIndexOfName(String address, int i) {
        Integer resultIndex = -1;
        if (StringUtils.isBlank(address)){
            return resultIndex;
        }
        //查找地址中含有的姓名是否存在过滤字段
        for (String nameCallFilter : NAME_CALL_FILTER) {
            if (address.indexOf(nameCallFilter) > 0){
                return resultIndex;
            }
        }
        //判断字符是否存在定义的称谓
        for (String call : NAME_CALL) {
            if (address.indexOf(call) > 0){
                return i;
            }
        }
        //判断字符中是否含有百家姓
        String nameFirst = address.substring(0, 1);
        if (address.length() <= NAME_MAX_LENGTH
        && address.length() > 1 && FAMILY_NAME_STR.indexOf(nameFirst) > 0){
            return i;
        }
        return resultIndex;
    }


    /**
     * 组装地址详情
     * @param addressList
     * @param parseResult
     */
    private static void packageDetail(List<String> addressList, ParseResult parseResult) {
        if (Objects.isNull(parseResult) || CollectionUtils.isEmpty(addressList)){
            throw new RuntimeException("地址解析参数不能为空");
        }
        addressList = new ArrayList<>(addressList);
        addressList.removeAll(Collections.singleton(""));
        StringBuilder detail = new StringBuilder();
        for (String address : addressList) {
            if (StringUtils.isNoneBlank(address)){
                String name = parseResult.getName();
                if (StringUtils.isBlank(name)){
                    break;
                }
                address = address.replaceAll(name,"");
                detail.append(address);
            }
        }

        if (StringUtils.isNoneBlank(detail)){
            parseResult.setDetail(detail.toString());
        }
    }

    /**
     * 获取匹配的数量
     * @param matcher
     * @return
     */
    private static List<String> matcherResult(Matcher matcher) {
        List<String> resultList = new ArrayList<>();
        while (matcher.find()){
            String group = matcher.group();
            if (StringUtils.isNoneBlank(group)){
                resultList.add(group);
            }
        }
        return resultList;
    }
}
