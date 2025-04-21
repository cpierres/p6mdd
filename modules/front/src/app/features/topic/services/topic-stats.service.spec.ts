import { TestBed } from '@angular/core/testing';

import { TopicStatsService } from './topic-stats.service';

describe('TopicStatsService', () => {
  let service: TopicStatsService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(TopicStatsService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
