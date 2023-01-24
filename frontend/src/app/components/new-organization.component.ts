import { Component, Input, ViewChild } from '@angular/core';
import { LocationService } from '../services/location.service';
import { SearchUserModalService } from './search-user-modal.component';
import { ignoreModalClose } from '@goraresult/yti-common-ui';
import { User } from '../entities/user';
import { OrganizationDetails } from '../entities/organization-details';
import { ActivatedRoute, Router } from '@angular/router';
import { ApiService } from '../services/api.service';
// import { NotificationDirective } from 'yti-common-ui/components/notification.component';
import { TranslateService } from '@ngx-translate/core';
import { OrganizationDetailsComponent } from './organization-details.component';
import { flatMap } from 'rxjs/operators';
import { EMPTY } from 'rxjs';
import { OrganizationTrans, UserWithRolesInOrganizations } from '../apina';

@Component({
  selector: 'app-new-organization',
  templateUrl: './new-organization.component.html',
  styleUrls: ['./new-organization.component.scss']
})
export class NewOrganizationComponent {

  organization = OrganizationDetails.empty();
  organizationAdminUsers: User[] = [];
  successfullySaved = false;
  parentOrganization: string;

  // @ViewChild('notification') notification: NotificationDirective;
  @ViewChild('details', { static: true }) details: OrganizationDetailsComponent;

  constructor(locationService: LocationService,
              private searchModal: SearchUserModalService,
              private apiService: ApiService,
              private router: Router,
              private activatedRoute: ActivatedRoute,
              private translateService: TranslateService) {

    locationService.atAddNewOrganization();

    const parentOrganziation$ = activatedRoute.params.pipe(flatMap(params => {
      if (params['parentId']) {
        return apiService.getOrganization(params['parentId']);
      }
      return EMPTY;
    }));

    parentOrganziation$.subscribe(organization => {
      this.organization = OrganizationDetails.emptyChildOrganization(organization.organization.id.toString());

      let parentOrganizationCurrTrans: OrganizationTrans | undefined = this.getOrganizationCurrentTranslation(organization.translations);
      if (parentOrganizationCurrTrans) {
        this.parentOrganization = parentOrganizationCurrTrans.name;
      }

      const adminUsers = organization.users
        .filter(user => user.roles.find(role => role === 'ADMIN'))
        .map(user => {
          const adminUser = new UserWithRolesInOrganizations();
          adminUser.firstName = user.user.firstName;
          adminUser.lastName = user.user.lastName;
          adminUser.email = user.user.email;

          return new User(adminUser);
          })
        this.organizationAdminUsers = adminUsers;
      });
  }

  private getOrganizationCurrentTranslation(translations: OrganizationTrans[]): OrganizationTrans | undefined {
    if (translations && translations.length) {
      let translation = translations.find(trans => trans.language === this.translateService.currentLang);
      if (translation) {
        return translation;
      } else {
        return translations[0];
      }
    }
    return;
  }

  get organizationAdminEmails(): string[] {
    return this.organizationAdminUsers.map(user => user.email);
  }

  isValid() {
    return this.details.isValid() && this.organizationAdminUsers.length > 0;
  }

  hasChanges() {
    return !this.successfullySaved && (this.details.hasChanges() || this.organizationAdminUsers.length > 0);
  }

  addUser() {
    this.searchModal.open(this.organizationAdminEmails)
      .then(user => this.organizationAdminUsers.push(user), ignoreModalClose);
  }

  saveOrganization() {
      this.apiService.createOrganization(this.organization, this.organizationAdminEmails).subscribe( {
        next: id => {
          this.successfullySaved = true;
          this.router.navigate(['/organization', id]);
        },
        // error: () => this.notification.showFailure(this.translateService.instant('Save failed'), 3000, 'left'),
        error: () => {}
      });
  }

  back() {
    this.router.navigate(['/']);
  }
}
