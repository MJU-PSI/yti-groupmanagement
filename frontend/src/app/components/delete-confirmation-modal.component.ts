import { Component, Injectable, Input } from '@angular/core';
import { NgbActiveModal, NgbModal } from '@ng-bootstrap/ng-bootstrap';

@Injectable()
export class DeleteConfirmationModalService {

  constructor(private modalService: NgbModal) {
  }

  open(username: string, useremail: string): Promise<any> {
    const modalRef = this.modalService.open(DeleteConfirmationModalComponent, { size: 'sm' });
    const instance = modalRef.componentInstance as DeleteConfirmationModalComponent;
    instance.username = username;
    instance.useremail = useremail;
    return modalRef.result;
  }
}

@Component({
  selector: 'app-delete-confirmation-modal',
  styleUrls: ['./delete-confirmation-modal.component.scss'],
  template: `
    <div class="modal-header modal-header-warning">
      <h4 class="modal-title">
        <a><i class="fa fa-times" (click)="cancel()"></i></a>
        <span translate>Confirm remove</span>
      </h4>
    </div>
    <div class="modal-body">
      <div class="row">
        <div class="col-md-12">
          
            <p translate>This user will be removed from organization after the changes are saved.</p>          
            <li>{{username}}  ({{useremail}})</li>
          <br>
          <span translate>Are you sure that you want to remove?</span>
        </div>
      </div>
    </div>
    <div class="modal-footer">
      <button type="button" class="btn btn-action confirm" (click)="confirm()" translate>Yes</button>
      <button type="button" class="btn btn-link cancel" (click)="cancel()" translate>Cancel</button>
    </div>
  `
})

export class DeleteConfirmationModalComponent {

  @Input() username: string;
  @Input() useremail: string;

  constructor(private modal: NgbActiveModal) {
  }

  cancel() {
    this.modal.dismiss('cancel');
  }

  confirm() {
    this.modal.close();
  }
}
