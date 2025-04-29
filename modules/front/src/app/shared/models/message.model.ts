import { MessageSeverity } from './MessageSeverity';

export type Message = {
  severity: MessageSeverity;
  text: string;
}
