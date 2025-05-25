import {Component, inject} from "@angular/core";
import {MessagesService} from "../../services/messages.service";
import {NgClass, NgIf} from "@angular/common";
import { MatIconModule } from "@angular/material/icon";

/**
 * deprecatead : plus utilisé car message est dans le service
 * (remplacé par snackbar)
 */
@Component({
    selector: 'messages',
    templateUrl: './messages.component.html',
    styleUrls: ['./messages.component.scss'],
    standalone: true,
    imports: [
      NgClass,
      MatIconModule,
      NgIf
    ]
})
export class MessagesComponent {

  messagesService = inject(MessagesService);

  message = this.messagesService.message;

  onClose() {
    this.messagesService.clear();
  }
}
