import { Injectable } from '@angular/core';
import {
  ConfigurationModel,
  CreateOrganization,
  EmailRole,
  FrontendEndpoint,
  Organization,
  OrganizationListItem,
  OrganizationTrans,
  OrganizationWithUsers,
  TokenModel,
  UpdateOrganization,
  UserRequestModel,
  UserRequestWithOrganization,
  UUID
} from '../apina';
import { Observable } from 'rxjs';
import { User } from '../entities/user';
import { OrganizationDetails } from '../entities/organization-details';
import { map } from 'rxjs/operators';

@Injectable()
export class ApiService {

  constructor(private endpoint: FrontendEndpoint) {
  }

  getUsersForOwnOrganizations(): Observable<User[]> {
    return this.endpoint.getUsersForOwnOrganizations().pipe(map(users =>
      users.map(userModel => new User(userModel))));
  }

  getTestUsers(): Observable<User[]> {
    return this.endpoint.getTestUsers().pipe(map(users =>
      users.map(userModel => new User(userModel))));
  }

  getUsers(): Observable<User[]> {
    return this.endpoint.getUsers().pipe(map(users =>
      users.map(userModel => new User(userModel))));
  }

  removeUser(userEmail: string): Observable<boolean> {
    return this.endpoint.removeUser(userEmail);
  }

  setSuperuser(userEmail: string): Observable<boolean> {
    return this.endpoint.setSuperuser(userEmail);
  }

  removeSuperuser(userEmail: string): Observable<boolean> {
    return this.endpoint.removeSuperuser(userEmail);
  }

  getOrganizationListOpt(showRemoved: boolean): Observable<OrganizationListItem[]> {
    return this.endpoint.getOrganizationsOpt(showRemoved);
  }

  getOrganizationList(): Observable<OrganizationListItem[]> {
    return this.endpoint.getOrganizations();
  }

  getOrganizationListWithChildren(): Observable<OrganizationListItem[]> {
    return this.endpoint.getOrganizationsWithChildren();
  }

  createOrganization(org: OrganizationDetails, adminsEmails: string[]): Observable<UUID> {

    const model = new CreateOrganization();

    model.url = org.url;
    model.adminUserEmails = adminsEmails;
    model.parentId = org.parentId;
    model.translations = org.translations;

    return this.endpoint.createOrganization(model);
  }

  updateOrganization(id: UUID, org: OrganizationDetails, userRoles: EmailRole[]): Observable<void> {

    const model = new UpdateOrganization();
    const organization = new Organization();
    organization.id = id;
    organization.url = org.url;
    organization.removed = org.removed;
    organization.parentId = org.parentId;
    model.organization = organization;
    model.userRoles = userRoles;
    const translations: OrganizationTrans[] = [];
    if (org.translations) {
      org.translations.forEach(t => {
        const organizationTrans = new OrganizationTrans();
        organizationTrans.organizationId = id;
        organizationTrans.name = t.name;
        organizationTrans.description = t.description;
        organizationTrans.language = t.language;
        translations.push(organizationTrans)
      })
      model.translations = translations;
    }

    return this.endpoint.updateOrganization(model);
  }

  getOrganization(id: UUID): Observable<OrganizationWithUsers> {
    return this.endpoint.getOrganization(id);
  }

  getAllRoles(): Observable<string[]> {
    return this.endpoint.getAllRoles();
  }

  getAllUserRequests(): Observable<UserRequestWithOrganization[]> {
    return this.endpoint.getAllUserRequests();
  }

  declineRequest(id: number): Observable<void> {
    return this.endpoint.declineUserRequest(id);
  }

  acceptRequest(id: number): Observable<void> {
    return this.endpoint.acceptUserRequest(id);
  }

  createRequest(req: UserRequestModel): Observable<void> {
    return this.endpoint.addUserRequest(req);
  }

  getConfiguration(): Observable<ConfigurationModel> {
    return this.endpoint.getConfiguration();
  }

  // createToken(): Observable<TokenModel> {
  //   return this.endpoint.createToken();
  // }

  // deleteToken(): Observable<boolean> {
  //   return this.endpoint.deleteToken();
  // }
}
