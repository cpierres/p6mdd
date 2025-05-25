import { ComponentFixture, TestBed } from '@angular/core/testing';

import { TopicListAllComponent } from './topic-list-all.component';

describe('TopicListAllComponent', () => {
  let component: TopicListAllComponent;
  let fixture: ComponentFixture<TopicListAllComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [TopicListAllComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(TopicListAllComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
