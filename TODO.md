## Open Problems

### Document
* All clocks
* Timing possibilities
* Synchronization Issues and coarse synch solutions/proposals

### Explore
* GPS visualization
 * Iterate
 * Fix (align to street?)
 * Project
 * When zoom in display more keypoints
* Drift between high-precision OS clock and GPS time
  *  In length runs
* Check network time (?)
* Thread pool

### Record
* Long-run tests
* Car, bike
* Multi-device
* Compass and Counter

-----
-----


## Fixes

### General
* Map video time with Location Points
* Add Graph with possible Location Point Switch (?)

### App
*  :zap: Fix camera bug (preview & record)
*  :zap: When start recording, save latest received location
* Handle time recording
* Consider adding multi-threading?
* Add 'Type' field in Location records

### Server / Processor
* Fix duration of first Sensor
* :zap: We assume every event is recorded in order (will **NOT** be the case if/when multi-threaded)
* Handle different timing (e.g. when recording from mobile ACC is current time, MAG is uptime)
* Decide on output format



### Client
* Display alternatives in video
* ~~Calculate possible trajectories~~
* Display available switches
