package rest.service;

import java.util.Collection;
import java.util.Optional;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import rest.domain.Farmaceut;
import rest.domain.Pregled;
import rest.domain.Zaposlenje;
import rest.domain.ZaposlenjeKorisnika;
import rest.dto.FarmaceutDTO;
import rest.dto.KorisnikDTO;
import rest.repository.FarmaceutRepository;
import rest.repository.LokacijaRepository;
import rest.repository.NotifikacijaRepository;
import rest.repository.PregledRepository;
import rest.repository.ZaposlenjeRepository;

@Service
@Transactional
public class FarmaceutServiceImpl implements FarmaceutService {

	private FarmaceutRepository farmaceutRepository;
	private ApotekaService apotekaService;
	private ZaposlenjeRepository zaposlenjeRepository;
	private LokacijaRepository lokacijaRepository;
	private PregledRepository pregledRepository;
	private NotifikacijaRepository notifikacijaRepository;
	
	@Autowired
	public FarmaceutServiceImpl(FarmaceutRepository imfr, ApotekaService as, ZaposlenjeRepository zr, LokacijaRepository lr, PregledRepository pr, NotifikacijaRepository nr) {
		this.farmaceutRepository = imfr;
		this.apotekaService = as;
		this.zaposlenjeRepository = zr;
		this.lokacijaRepository = lr;
		this.pregledRepository = pr;
		this.notifikacijaRepository = nr;
	}

	@Override
	public Collection<Farmaceut> findAll() {
		Collection<Farmaceut> users = farmaceutRepository.findAll();
		return users;
	}

	@Override
	public Farmaceut findOne(int id) {
		Optional<Farmaceut> user = farmaceutRepository.findById(id);
		if (user.isPresent()) {
			return user.get();
		}
		return null;
	}

	@Override
	@Transactional
	public Farmaceut create(FarmaceutDTO farmaceut, int idApoteke) throws Exception {
		lokacijaRepository.save(farmaceut.getLokacija());
		Farmaceut f = new Farmaceut();
		f.setIme(farmaceut.getIme());
		f.setPrezime(farmaceut.getPrezime());
		f.setUsername(farmaceut.getUsername());
		f.setEmail(farmaceut.getEmail());
		f.setTelefon(farmaceut.getTelefon());
		f.setLokacija(farmaceut.getLokacija());
		f.setZaposlenjeKorisnika(farmaceut.getZaposlenjeKorisnika());
		f.setPassword(farmaceut.getNoviPassw());
		f.setLoggedBefore(false);
		f.setZaposlenjeKorisnika(ZaposlenjeKorisnika.FARMACEUT);
		f.setBrojOcena(0);
		f.setSumaOcena(0);
		f.setOcena(0);


		Zaposlenje zaposlenje = new Zaposlenje(farmaceut.getPocetakRadnogVremena(), farmaceut.getKrajRadnogVremena(), apotekaService.getForAdmin(idApoteke), f);
		zaposlenjeRepository.save(zaposlenje);
		f.setZaposlenje(zaposlenje);
		Farmaceut savedUser = farmaceutRepository.save(f);
		return savedUser;
	}

	@Override
	public Farmaceut update(KorisnikDTO user) throws Exception {
		Farmaceut userToUpdate = findOne(user.getId());
		
		
		if (userToUpdate == null) {
			throw new Exception("Trazeni entitet nije pronadjen.");
		}
		userToUpdate.setIme(user.getIme());
		userToUpdate.setPrezime(user.getPrezime());
		userToUpdate.setUsername(user.getUsername());
		userToUpdate.setTelefon(user.getTelefon());
		userToUpdate.setLokacija(user.getLokacija());
		
		Farmaceut updatedUSer = farmaceutRepository.save(userToUpdate);
		return updatedUSer;
	}

	@Override
	public void delete(int id) {
		farmaceutRepository.deleteById(id);
	}

	@Override
	public Collection<Farmaceut> findAllForPharmacy(int id) {
		return farmaceutRepository.getWithEmployments(id);
	}

	@Override
	public boolean checkIfPharmacistHasAppointments(int pharmacistId, int pharmacyId) {
		Collection<Pregled> appointments = pregledRepository.getScheduledAppointments(pharmacistId, pharmacyId);
		if (appointments.size() == 0) {
			return false;
		}

		return true;
	}

	@Override
	public void deletePharmacist(int pharmacistId) {
		notifikacijaRepository.deleteNotificationsOfUser(pharmacistId);
		farmaceutRepository.deleteById(pharmacistId);
		zaposlenjeRepository.deleteForPharmacist(pharmacistId);
	}

}