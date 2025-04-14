import {Injectable, signal} from "@angular/core";
import {Message, MessageSeverity} from "../components/models/message.model";

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

  showMessage(text:string, severity: MessageSeverity) {
    this.#messageSignal.set({
      text, severity
    })
  }

  clear() {
    this.#messageSignal.set(null);
  }

}
