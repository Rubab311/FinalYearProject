package com.example.ikhwa.modules;

public class FamilyMember {

    private String memberId;
    private String name;
    private String age;
    private String gender;
    private String relationship;

    private String father_name;
    private String phone;
    private String address;
    private String email;

    public FamilyMember() {
    }

    public FamilyMember(String memberId,
                        String name,
                        String age,
                        String gender,
                        String relationship,
                        String father_name,
                        String phone,
                        String address,
                        String email) {

        this.memberId = memberId;
        this.name = name;
        this.age = age;
        this.gender = gender;
        this.relationship = relationship;
        this.father_name = father_name;
        this.phone = phone;
        this.address = address;
        this.email = email;
    }

    public String getMemberId() {
        return memberId;
    }

    public void setMemberId(String memberId) {
        this.memberId = memberId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAge() {
        return age;
    }

    public void setAge(String age) {
        this.age = age;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getRelationship() {
        return relationship;
    }

    public void setRelationship(String relationship) {
        this.relationship = relationship;
    }

    public String getFather_name() {
        return father_name;
    }

    public void setFather_name(String father_name) {
        this.father_name = father_name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}