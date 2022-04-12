import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AllTopicComponent } from './all-topic.component';

describe('AllTopicComponent', () => {
  let component: AllTopicComponent;
  let fixture: ComponentFixture<AllTopicComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ AllTopicComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(AllTopicComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
