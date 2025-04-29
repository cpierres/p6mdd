import {Injectable, signal} from "@angular/core";
import {Message} from "../models/message.model";
import {MessageSeverity} from '../models/MessageSeverity';

@Injectable({
  providedIn: 'root'
})
/**
 * Dans le projet 2 OCR, j'avais créé un service messages réactif basé sur BehaviorSubject.
 * Celui-ci est basé sur Signal.
 */
export class MessagesService {

  #messageSignal = signal<Message | null>(null);

  message = this.#messageSignal.asReadonly();

  showMessage(text: string, severity: MessageSeverity = 'warning') {
    this.#messageSignal.set({
      text, severity
    })
  }

  clear() {
    this.#messageSignal.set(null);
  }

}
