INSERT INTO "user" (id,email,firstname,lastname,superuser) VALUES ('4ce70937-6fa4-49af-a229-b5f10328adb8','admin@localhost','Admin','User',true);
INSERT INTO organization (id,url) VALUES ('7d3a3c00-5a6b-489b-a3ed-63bb58c26a63','https://www.gov.si/drzavni-organi/ministrstva/ministrstvo-za-digitalno-preobrazbo/');
INSERT INTO organization_trans (organization_id,language,name) VALUES ('7d3a3c00-5a6b-489b-a3ed-63bb58c26a63','en','Ministry of Digital Transformation');
INSERT INTO organization_trans (organization_id,language,name) VALUES ('7d3a3c00-5a6b-489b-a3ed-63bb58c26a63','sl','Ministrstvo za digitalno preobrazbo');
INSERT INTO user_organization VALUES ('7d3a3c00-5a6b-489b-a3ed-63bb58c26a63','ADMIN','4ce70937-6fa4-49af-a229-b5f10328adb8');
