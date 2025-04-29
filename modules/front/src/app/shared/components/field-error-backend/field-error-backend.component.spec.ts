import { ComponentFixture, TestBed } from '@angular/core/testing';

import { FieldErrorBackendComponent } from './field-error-backend.component';

describe('FieldErrorBackendComponent', () => {
  let component: FieldErrorBackendComponent;
  let fixture: ComponentFixture<FieldErrorBackendComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [FieldErrorBackendComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(FieldErrorBackendComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
