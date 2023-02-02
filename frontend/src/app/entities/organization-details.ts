import { Localizable } from '@mju-psi/yti-common-ui';
import { Organization, OrganizationListItem, OrganizationTrans, UUID } from '../apina';
import { availableLanguages } from '@mju-psi/yti-common-ui';

export class OrganizationDetails {
  availableLanguages: any[];

  constructor(public url: string,
              public removed: boolean,
              public parentId: UUID,
    public childOrganizations: OrganizationListItem[],
    public translations: OrganizationTrans[]) {
    this.availableLanguages = availableLanguages;
  }

  static empty() {
    return new OrganizationDetails('', false, '', [], this.getEmptyTranslations());
  }

  static getEmptyTranslations(): OrganizationTrans[] {
    let translations: OrganizationTrans[] = [];
    availableLanguages.forEach(language => {
      const orgTrans = new OrganizationTrans();
      orgTrans.language = language.code;
      translations.push(orgTrans);
    });
    return translations;
  }

  static emptyChildOrganization(parentId: UUID) {
    return new OrganizationDetails('', false, parentId, [], this.getEmptyTranslations());
  }

  static fromOrganization(model: Organization, childOrganizations: OrganizationListItem[], translations: OrganizationTrans[]) {
    return new OrganizationDetails(
      model.url,
      model.removed,
      model.parentId,
      childOrganizations,
      translations
    );
  }

  get name(): Localizable {
    return this.translations.reduce(
      (obj, item) => Object.assign(obj, { [item.language]: item.name }), {});
  }

  get description(): Localizable {
    return this.translations.reduce(
      (obj, item) => Object.assign(obj, { [item.language]: item.description }), {});
  }

  clone() {
    return new OrganizationDetails(
      this.url,
      this.removed,
      this.parentId,
      this.childOrganizations,
      this.translations
    );
  }
}
