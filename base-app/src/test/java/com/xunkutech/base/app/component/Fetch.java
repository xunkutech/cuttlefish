package com.xunkutech.base.app.component;

import com.xunkutech.base.model.JsonSerializable;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

public class Fetch {
    /**
     * @param args
     */
    public static void main(String[] args) throws Exception {

        Connection.Response res = Jsoup.connect("https://www.361n.com/manage/index.php?s=/Admin/Public/checklogin")
                .data("account", "szgx", "password", "123123","dosubmit", "登录", "crsf","bG9naW5mcm9tOndlYjoxNTAzMjc1MDMz")
                .cookie("PHPSESSID", "1c1qa7shavv0gdf5bfkdlosu83")
                .method(Connection.Method.POST)
                .execute();

//        Document doc = res.parse();
        String sessionId = res.cookie("PHPSESSID");

        //PHPSESSID=vspbkv5vkc5n1r9gq53pfg7is2; _currentUrl_=%2Fmanage%2Findex.php%3Fs%3D%2FCustomer%2Findex
        for (int i = 238740; i > 238739; i--) {
            try {
                Document doc = Jsoup
                        .connect("https://www.361n.com/manage/index.php?s=/Customer/edit&id=" + i)
                        .cookie("PHPSESSID", "1c1qa7shavv0gdf5bfkdlosu83")
                        .cookie("_currentUrl_", "%2Fmanage%2Findex.php%3Fs%3D%2FCustomer%2Findex")
                        .get();

                Customer.CustomerBuilder customerBuilder = Customer.builder();
                //name
//        System.out.println(doc.getElementById("customer_name").val());
                customerBuilder.name(doc.getElementById("customer_name").val());

                //sex
                for (Element element : doc.getElementById("sex").select("option")) {
                    if ("selected".equals(element.attr("selected")))
                        customerBuilder.sex(element.text());
//                System.out.println(element.text());
                }

                //age
//        System.out.println(doc.getElementById("age").val());
                customerBuilder.age(doc.getElementById("age").val());

                //marriage
                for (Element element : doc.getElementById("married").select("option")) {
                    if ("selected".equals(element.attr("selected")))
                        customerBuilder.marriage(element.text());
//                System.out.println(element.text());
                }

                //cardtype
                for (Element element : doc.getElementById("cardtype").select("option")) {
                    if ("selected".equals(element.attr("selected")))
//                System.out.println(element.text());
                        customerBuilder.idType(element.text());
                }

                //cardcode
//        System.out.println(doc.getElementById("customer_cardcode").val());
                customerBuilder.idCode(doc.getElementById("customer_cardcode").val());

                //childage
//        System.out.println(doc.getElementById("childage").val());
                customerBuilder.ageOfChildren(doc.getElementById("childage").val());

                //region
//        System.out.println(doc.getElementById("customer_region").val());
                customerBuilder.province(doc.getElementById("customer_region").val());

                //domicile
//        System.out.println(doc.getElementById("domicile").val());
                customerBuilder.domicile(doc.getElementById("domicile").val());

                //domicile code
//        System.out.println(doc.getElementById("domicilecode").val());
                customerBuilder.domicileCode(doc.getElementById("domicilecode").val());

                //situation
                for (Element element : doc.getElementById("situation").select("option")) {
                    if ("selected".equals(element.attr("selected")))
//                System.out.println(element.text());
                        customerBuilder.residenceType(element.text());
                }

                //monthfee
//        System.out.println(doc.getElementById("monthfee").val());
                customerBuilder.monthRentalFee((doc.getElementById("monthfee").val()));

                //dname
//        System.out.println(doc.getElementById("dname").val());
                customerBuilder.residenceOwner(doc.getElementById("dname").val());

                //address
//        System.out.println(doc.getElementById("address").val());
                customerBuilder.residence(doc.getElementById("address").val());

                //tel
//        System.out.println(doc.getElementById("tel").val());
                customerBuilder.tel(doc.getElementById("tel").val());

                //area
                for (Element element : doc.getElementById("area").select("option")) {
                    if ("selected".equals(element.attr("selected")))
//                System.out.println(element.text());
                        customerBuilder.homeAway(element.text());
                }

                //mobile
//        System.out.println(doc.getElementById("mobile").val());
                customerBuilder.mobile(doc.getElementById("mobile").val());


                //mobile_used
//        System.out.println(doc.getElementById("mobile_used").val());
                customerBuilder.mobileUsedYears(doc.getElementById("mobile_used").val());

                //isrealname
                for (Element element : doc.getElementById("isrealname").select("option")) {
                    if ("selected".equals(element.attr("selected")))
//                System.out.println(element.text());
                        customerBuilder.mobileRealName(element.text());
                }

                //education
                for (Element element : doc.getElementById("education").select("option")) {
                    if ("selected".equals(element.attr("selected")))
//                System.out.println(element.text());
                        customerBuilder.diploma(element.text());
                }

                //school
//        System.out.println(doc.getElementById("school").val());
                customerBuilder.graduateSchool(doc.getElementById("school").val());

                //bankaccount
//        System.out.println(doc.getElementById("bankaccount").val());
                customerBuilder.bankAccount(doc.getElementById("bankaccount").val());

                //bankname
//        System.out.println(doc.getElementById("bankname").val());
                customerBuilder.bankName(doc.getElementById("bankname").val());

                //introducer
//        System.out.println(doc.getElementById("introducer").val());
                customerBuilder.referrer(doc.getElementById("introducer").val());

                //introducer_mobile
//        System.out.println(doc.getElementById("introducer_mobile").val());
                customerBuilder.referrerMobile(doc.getElementById("introducer_mobile").val());

                //remarks
//        System.out.println(doc.getElementById("remarks").text());
                customerBuilder.memo(doc.getElementById("remarks").text());

                //comname
//        System.out.println(doc.getElementById("comname").val());
                customerBuilder.employer(doc.getElementById("comname").val());

                //branch
//        System.out.println(doc.getElementById("branch").val());
                customerBuilder.dept(doc.getElementById("branch").val());

                //office
//        System.out.println(doc.getElementById("office").val());
                customerBuilder.position(doc.getElementById("office").val());

                //comaddress
//        System.out.println(doc.getElementById("comaddress").val());
                customerBuilder.workAddress(doc.getElementById("comaddress").val());

                //zipcode
//        System.out.println(doc.getElementById("zipcode").val());
                customerBuilder.workZipCode(doc.getElementById("zipcode").val());

                //comtel
//        System.out.println(doc.getElementById("comtel").val());
                customerBuilder.workTel(doc.getElementById("comtel").val());

                //comtel_ext
//        System.out.println(doc.getElementById("comtel_ext").val());
                customerBuilder.workTelExt(doc.getElementById("comtel_ext").val());

                //hrtel
//        System.out.println(doc.getElementById("hrtel").val());
                customerBuilder.workHrTel(doc.getElementById("hrtel").val());

                //com[workyear]
//        System.out.println(doc.getElementById("com[workyear]").val());
                customerBuilder.employYears(doc.getElementById("com[workyear]").val());

                //com[salaries]
//        System.out.println(doc.getElementById("com[salaries]").val());
                customerBuilder.salary((doc.getElementById("com[salaries]").val()));

                //fundname
//        System.out.println(doc.getElementById("fundname").val());
                customerBuilder.accumulationFundAccount(doc.getElementById("fundname").val());

                //fundpwd
//        System.out.println(doc.getElementById("fundpwd").val());
                customerBuilder.accumulationFundPassword(doc.getElementById("fundpwd").val());

                //comtype
                for (Element element : doc.getElementById("comtype").select("option")) {
                    if ("selected".equals(element.attr("selected")))
//                System.out.println(element.text());
                        customerBuilder.employerType(element.text());
                }

                List<Contact> contacts = new ArrayList<>();
                //contactsinfo
                for (Element element : doc.getElementById("contactsInfo").select("input[name=linkmanid[]]")) {
                    Contact.ContactBuilder contactBuilder = Contact.builder();
                    contactBuilder.id(element.val());
//            System.out.println(element.val());
                    element = element.nextElementSibling();
                    contactBuilder.relation(element.getElementById("relation").val());
//            System.out.println(element.getElementById("relation").val());
                    contactBuilder.name(element.getElementById("linkname").val());
//            System.out.println(element.getElementById("linkname").val());
//            System.out.println(element.getElementById("mobile").val());
                    contactBuilder.mobile(element.getElementById("mobile").val());
//            System.out.println(element.nextElementSibling().getElementById("work").val());
                    contactBuilder.employment(element.nextElementSibling().getElementById("work").val());
                    contacts.add(contactBuilder.build());
                }
                customerBuilder.contacts(contacts);


                //linkman_other
//        System.out.println(doc.getElementById("linkman_other").text());
                customerBuilder.otherContact(doc.getElementById("linkman_other").text());

                //credit_card
                List<CreditCard> creditCards = new ArrayList<>();
                for (Element element : doc.select("div.cardcode")) {
                    CreditCard.CreditCardBuilder creditCardBuilder = CreditCard.builder();
//            System.out.println(element.getElementById("cardcode1").val());
                    creditCardBuilder.bankName(element.getElementById("cardcode1").val());
                    element = element.nextElementSibling();
//            System.out.println(element.getElementById("maxfee").val());
                    creditCardBuilder.creditLine((element.getElementById("maxfee").val()));
                    element = element.nextElementSibling();
//            System.out.println(element.getElementById("usedfee").val());
                    creditCardBuilder.usedLine((element.getElementById("usedfee").val()));
                    creditCards.add(creditCardBuilder.build());
                }
                customerBuilder.creditCards(creditCards);

                //extendcredit
                List<LingYongDai> lingYongDais = new ArrayList<>();
                for (Element element : doc.select("div.extendcreditname")) {
                    LingYongDai.LingYongDaiBuilder lingYongDaiBuilder = LingYongDai.builder();
//            System.out.println(element.getElementById("creditname1").val());
                    lingYongDaiBuilder.loaner(element.getElementById("creditname1").val());
                    element = element.nextElementSibling();
//            System.out.println(element.getElementById("creditfee1").val());
                    lingYongDaiBuilder.debt((element.getElementById("creditfee1").val()));
                    lingYongDais.add(lingYongDaiBuilder.build());
                }
                customerBuilder.lingYongDais(lingYongDais);

                //othercredit
                List<Loaner> loaners = new ArrayList<>();
                for (Element element : doc.select("div.othercreditname")) {
                    Loaner.LoanerBuilder loanerBuilder = Loaner.builder();
//            System.out.println(element.getElementById("creditname3").val());
                    loanerBuilder.loaner(element.getElementById("creditname3").val());
                    element = element.nextElementSibling();
//            System.out.println(element.getElementById("creditfee3").val());
                    loanerBuilder.debt((element.getElementById("creditfee3").val()));
                    loaners.add(loanerBuilder.build());
                }
                customerBuilder.loaners(loaners);

                Customer customer = customerBuilder.build();

                System.out.println(customer.printJson());

                String fileName = String.format("%s%06d-%s-%s", "/home/jason/51haidai/customers/", i, customer.getIdCode(), customer.getMobile());
                FileWriter fileWriter = new FileWriter(fileName);
                BufferedWriter bf = new BufferedWriter(fileWriter);
                bf.write(customer.printJson());
                bf.newLine();
                bf.flush();
                bf.close();
                Thread.sleep(50);
            } catch (Exception e) {
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
            }
        }
    }

    @Getter
    @Setter
    @Builder
    public static class Customer implements JsonSerializable {
        String name;//姓名
        String sex;//性别
        String age;//年龄
        String marriage;//婚姻状况
        String idType;//证件类型
        String idCode;//证件号码
        String ageOfChildren;//子女年龄
        String province;//省市区
        String domicile;//户籍地址
        String domicileCode;//户籍所在地水电户号
        String residenceType;//居住地类型：租房、自住
        String monthRentalFee;//居住地每月租金
        String residenceOwner;//居住地房东
        String residence;//居住地
        String homeAway;//本地外地
        String tel;//居住地电话
        String mobile;//移动电话
        String mobileUsedYears;//移动电话使用年限
        String mobileRealName;//移动电话是否实名
        String diploma;//学历
        String graduateSchool;//毕业学校
        String bankAccount;//收款银行帐号
        String bankName;//收款银行名称
        String referrer;//推荐人姓名
        String referrerMobile;//推荐人手机号码
        String memo;//备注
        String employer;//工作单位
        String dept;//所在部门
        String position;//职务
        String workAddress;//工作地点
        String workZipCode;//工作地邮编
        String workTel;//公司电话
        String workTelExt;//公司电话分机
        String workHrTel;//公司人事电话
        String employYears;//在职年限
        String salary;//工资
        String accumulationFundAccount;//公积金账号
        String accumulationFundPassword;//公积金账号密码
        String employerType;//公司类型
        List<Contact> contacts;//联系人列表
        String otherContact;//其他联系人
        List<CreditCard> creditCards;//信用卡列表
        List<LingYongDai> lingYongDais;//零用贷记录
        List<Loaner> loaners;//其他贷款记录
    }

    @Getter
    @Setter
    @Builder
    public static class Contact {
        String id;
        String relation;
        String name;
        String mobile;
        String employment;
    }

    @Getter
    @Setter
    @Builder
    public static class CreditCard {
        String bankName;
        String creditLine;
        String usedLine;
    }

    @Getter
    @Setter
    @Builder
    public static class LingYongDai {
        String loaner;
        String debt;
    }

    @Getter
    @Setter
    @Builder
    public static class Loaner {
        String loaner;
        String debt;
    }
}
