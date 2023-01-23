CREATE TABLE organization_trans
(
  organization_id      UUID         NOT NULL REFERENCES organization,
  language             TEXT NOT NULL,
  name                 TEXT,
  description          TEXT,
  PRIMARY KEY (organization_id, language)
);

INSERT INTO organization_trans (organization_id, language, name, description) SELECT id, 'en', name_en, description_en FROM organization;
INSERT INTO organization_trans (organization_id, language, name, description) SELECT id, 'fi', name_fi, description_fi FROM organization;
INSERT INTO organization_trans (organization_id, language, name, description) SELECT id, 'sv', name_sv, description_sv FROM organization;

ALTER TABLE organization DROP COLUMN name_en;
ALTER TABLE organization DROP COLUMN name_fi;
ALTER TABLE organization DROP COLUMN name_sv;
ALTER TABLE organization DROP COLUMN description_en;
ALTER TABLE organization DROP COLUMN description_fi;
ALTER TABLE organization DROP COLUMN description_sv;