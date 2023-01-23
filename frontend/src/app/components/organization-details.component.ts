import { Component, Input, ViewChild } from '@angular/core';
import { OrganizationDetails } from '../entities/organization-details';
import { AuthorizationManager } from '../services/authorization-manager.service';
import { NgForm } from '@angular/forms';
import { availableLanguages } from '@goraresult/yti-common-ui';
import { OrganizationTrans } from '../apina';
import { OrganizationTransWithName } from '../entities/organization-trans-with-name';

@Component({
  selector: 'app-organization-details',
  exportAs: 'details',
  templateUrl: './organization-details.component.html',
  styleUrls: ['./organization-details.component.scss']
})
export class OrganizationDetailsComponent {

  availableLanguages: any[];
  languages: OrganizationTransWithName[];

  @Input()
  organization: OrganizationDetails;

  @Input()
  editing: boolean;

  @Input()
  parentOrganization: string;

  @ViewChild('form', { static: true }) form: NgForm;

  constructor(private authorizationManager: AuthorizationManager) {
    this.availableLanguages = availableLanguages;
    this.languages = [];

  }

  ngOnInit(): void {
    this.languages = [];
    this.availableLanguages.forEach(language => {
      const ot = new OrganizationTransWithName();
      ot.language = language.code;
      ot.languageName = language.name;
      if (language.code && this.organization && this.organization.translations) {
        let translation = this.getTranslation(this.organization.translations, language.code);
        if (translation) {
          ot.name = translation.name;
          ot.description = translation.description;
        }
      }
      this.languages.push(ot);
    });
    this.organization.translations = this.languages;
  }

  private getTranslation(translations: OrganizationTrans[], language: string): any {
    return translations.find(obj => {
      return obj.language === language;
    })
  }

  hasChanges() {
    return this.form.dirty;
  }

  reset() {
    // FIXME: no idea why this timeout hack is needed
    setTimeout(() => this.form.resetForm());
  }

  canRemoveOrganization(): boolean {
    return this.authorizationManager.canRemoveOrganization();
  }

  isValid() {
    return this.form.valid;
  }
}
