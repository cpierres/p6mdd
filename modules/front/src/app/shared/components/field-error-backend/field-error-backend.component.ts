import {Component, Input} from '@angular/core';
import {MatIconModule} from '@angular/material/icon';
import {NgClass} from '@angular/common';
import {FieldErrorDetail} from '../../interfaces/FieldErrorDetail';

@Component({
  selector: 'app-field-error-backend',
  imports: [
    MatIconModule, NgClass
  ],
  templateUrl: './field-error-backend.component.html',
  styleUrl: './field-error-backend.component.scss'
})
export class FieldErrorBackendComponent {
  @Input() error?: FieldErrorDetail;

  getIconForSeverity(severity?: string): string {
    switch (severity) {
      case 'error':
        return 'error';
      case 'warning':
        return 'warning';
      case 'info':
        return 'info';
      case 'success':
        return 'check_circle';
      default:
        return 'error';
    }
  }
}
