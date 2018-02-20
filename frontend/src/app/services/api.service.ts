import { Injectable } from '@angular/core';
import {
  CreateOrganization,
  EmailRole,
  FrontendEndpoint,
  Organization,
  OrganizationListItem,
  OrganizationWithUsers,
  UpdateOrganization,
  UserRequestModel,
  UserRequestWithOrganization,
  UUID
} from '../apina';
import { Observable } from 'rxjs/Observable';
import { User } from '../entities/user';
import { OrganizationDetails } from '../entities/organization-details';

@Injectable()
export class ApiService {

  constructor(private endpoint: FrontendEndpoint) {
  }

  getUsersForOwnOrganizations(): Observable<User[]> {
    return this.endpoint.getUsersForOwnOrganizations().map(users =>
      users.map(userModel => new User(userModel)));
  }

  getUsers(): Observable<User[]> {
    return this.endpoint.getUsers().map(users =>
      users.map(userModel => new User(userModel)));
  }

  getOrganizationList(): Observable<OrganizationListItem[]> {
    return this.endpoint.getOrganizations();
  }

  createOrganization(org: OrganizationDetails, adminsEmails: string[]): Observable<UUID> {

    const model = new CreateOrganization();

    model.url = org.url;
    model.nameEn = org.nameEn;
    model.nameFi = org.nameFi;
    model.nameSv = org.nameSv;
    model.descriptionEn = org.descriptionEn;
    model.descriptionFi = org.descriptionFi;
    model.descriptionSv = org.descriptionSv;
    model.adminUserEmails = adminsEmails;

    return this.endpoint.createOrganization(model);
  }

  updateOrganization(id: UUID, org: OrganizationDetails, userRoles: EmailRole[]): Observable<void> {

    const model = new UpdateOrganization();
    const organization = new Organization();
    organization.id = id;
    organization.url = org.url;
    organization.nameEn = org.nameEn;
    organization.nameFi = org.nameFi;
    organization.nameSv = org.nameSv;
    organization.descriptionEn = org.descriptionEn;
    organization.descriptionFi = org.descriptionFi;
    organization.descriptionSv = org.descriptionSv;
    organization.removed = org.removed;
    model.organization = organization;
    model.userRoles = userRoles;


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
}
