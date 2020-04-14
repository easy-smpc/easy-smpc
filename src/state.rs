use super::secret;

pub enum State {
    None,
    Creating,
    SendingInput,
    SendingResult,
    RecievingInput,
    RecievingResult,
    Done,
}

pub struct Bin {
    name: String,
    shares: Vec<secret::Share>,
    complete: bool,
    result: Option<secret::ValueType>,
}

impl Bin {
    pub fn new(name: &str) -> Self {
        Bin {
            name: name.to_string(),
            shares: Vec::new(),
            complete: false,
            result: None,
        }
    }
}

#[derive(Debug, Clone)]
pub struct Participant {
    name: String,
    email: String,
    unsent_messages: Vec<String>,
}
pub struct ProgramModel {
    state: State,
    name: String,
    bins: Vec<Bin>,
    participants: Vec<Participant>,
    saved: bool,
    share_generator: secret::SharingType,
}

impl ProgramModel {
    pub fn new() -> Self {
        Self {
            state: State::None,
            name: String::new(),
            bins: Vec::new(),
            participants: Vec::new(),
            saved: false,
            share_generator: secret::SharingType::None,
        }
    }

    pub fn add_bin(& mut self, name: &str) {
        self.bins.push(Bin::new(name));
    }
    pub fn add_participant(& mut self, name: &str, email: &str) {
        self.participants.push(Participant {
            name: name.to_string(),
            email: email.to_string(),
            unsent_messages: Vec::new(),
        });
    }
    pub fn get_participant(&self, id: usize) -> &Participant {
        self.participants.get(id).expect("Unknown participant ID")
    }
    pub fn get_bin(&self, id: usize) -> &Bin{
        self.bins.get(id).expect("Unknown Bin ID")
    }
    pub fn get_num_bins(&self) -> usize{
        self.bins.len()
    }
    pub fn get_num_participants(&self) -> usize {
        self.participants.len()
    }
    pub fn set_bin_value(& mut self, bin: usize, value: secret::ValueType) {
        let shares = match &self.share_generator {
            secret::SharingType::ArithmeticSharing(i) => i.share(value),
            secret::SharingType::ShamirsSharing(i) => i.share(value),
            secret::SharingType::None => panic!("This should never be reached!")
        };
        match self.bins.get_mut(bin) {
            Some(i) => i.shares = shares,
            None => panic!("Unknown Bin ID"),
        };
    }
}
